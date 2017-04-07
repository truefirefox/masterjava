package ru.javaops.masterjava.export;

import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.GroupType;
import ru.javaops.masterjava.persist.model.Project;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by val on 2017-04-06.
 */
@Slf4j
public class ProjectGroupExport {
    private static final int NUMBER_THREADS = 4;
    private final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);
    private final GroupDao groupDao = DBIProvider.getDao(GroupDao.class);
    private final ProjectDao projectDao = DBIProvider.getDao(ProjectDao.class);


    public List<String> process(final StaxStreamProcessor processor, int chunkSize) throws Exception {

        return new Callable<List<String>>() {

            @Override
            public List<String> call() throws Exception {

                List<String> futures = new ArrayList<>();
                int id = projectDao.getSeqAndSkip(chunkSize);

                // Projects loop
                String tag = processor.doUntilAny(XMLEvent.START_ELEMENT, "Project", "Cities");
                while (tag != null) {

                    if (tag.equals("Cities")) break;

                    final String name = processor.getAttribute("name");
                    processor.doUntil(XMLEvent.START_ELEMENT, "description");
                    final String description = processor.getReader().getElementText();

                    final Project project = new Project(id++, description, name);

                    // Groups loop
                    List<Group> groups = new ArrayList<>();
                    while ((tag = processor.doUntilAny(XMLEvent.START_ELEMENT, "Project", "Group", "Cities")) != null) {

                        if (tag.equals("Cities") || tag.equals("Project")) break;

                        final String groupName = processor.getAttribute("name");
                        final GroupType type = GroupType.valueOf(processor.getAttribute("type"));

                        groups.add(new Group(id++, groupName, type, project.getId()));

                        if (groups.size() == chunkSize - 1) {
                            id = projectDao.getSeqAndSkip(chunkSize);
                        }
                    }
                    project.setGroups(groups);

                    futures.addAll(submit(project));
                }
                return futures;
            }

            private List<String> submit(Project project) throws Exception {
                Future<List<String>> chunkFuture = executorService.submit(() -> {
                    List<String> stringsResult = new ArrayList<>();

                    //add project to DB
                    //insertWitId on conflict return id for row already present in DB
                    int resultId = projectDao.insertWitId(project);
                    if (resultId != project.getId()) {
                        stringsResult.add("project with name " + project.getName() + " already present");
                        //change projectId for project & groups to actual
                        project.setId(resultId);
                        project.getGroups().forEach(g -> g.setProjectId(resultId));
                    }

                    //add groups to DB by chunk
                    List<String> conflictNames = new ArrayList<>();
                    int groupSize = project.getGroups().size();
                    for (int i = 0; i < groupSize / chunkSize + 1; i++) {
                        int start = i * chunkSize;
                        int end = (i + 1) * chunkSize > groupSize ? groupSize : (i + 1) * chunkSize;
                        List<Group> groupsChunk = project.getGroups().subList(start, end);
                        conflictNames.addAll(groupDao.insertAndGetConflictName(groupsChunk));
                    }
                    conflictNames.forEach(s -> stringsResult.add("   - group with name " + s + " for project " + project.getName() + " already present"));
                    return stringsResult;
                });
                log.info("Submit project " + project.getName() + " with " + project.getGroups().size() + " groups");
                //return List<String> with fail messages
                return chunkFuture.get();
            }
        }.call();
    }
}
