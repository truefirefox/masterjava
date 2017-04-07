package ru.javaops.masterjava.export;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.events.XMLEvent;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * gkislin
 * 14.10.2016
 */
@Slf4j
public class UserExport {
    private final UserDao userDao = DBIProvider.getDao(UserDao.class);
    private final GroupDao groupDao = DBIProvider.getDao(GroupDao.class);

    public List<String> process(final ExecutorService executorService,
                                final StaxStreamProcessor processor,
                                int chunkSize) throws Exception {

        return new Callable<List<String>>() {

            @Override
            public List<String> call() throws Exception {
                List<String> futures = new ArrayList<>();

                int id = userDao.getSeqAndSkip(chunkSize);
                Map<User, List<String>> chunk = new HashMap<>(chunkSize);

                while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
                    final String email = processor.getAttribute("email");
                    final String city = processor.getAttribute("city");
                    String groupRefs = processor.getAttribute("groupRefs");
                    final UserFlag flag = UserFlag.valueOf(processor.getAttribute("flag"));
                    final String fullName = processor.getText();
                    final User user = new User(id++, fullName, email, flag, city);
                    chunk.put(user, Splitter.on(' ').splitToList(groupRefs));
                    if (chunk.size() == chunkSize) {
                        futures.addAll(submit(chunk));
                        chunk = new HashMap<>(chunkSize);
                        id = userDao.getSeqAndSkip(chunkSize);
                    }
                }

                if (!chunk.isEmpty()) {
                    futures.addAll(submit(chunk));
                }
                return futures;
            }

            private List<String> submit(Map<User, List<String>> chunk) throws Exception {

                Future<List<String>> chunkFuture = executorService.submit(
                        () -> {
                            List<String> failed = new ArrayList<>();
                            List<User> users = userDao.insertAndGetConflicts(new ArrayList<>(chunk.keySet()));
                            users.forEach(u -> {
                                failed.add("User with email = " + u.getEmail() + " already present");
                                chunk.remove(u);
                            });
                            // don't need failed messages for groups because "DELETE ON CASCADE"
                            chunk.forEach((User u, List<String> groups) -> groupDao.updateGroups(u.getEmail(), groups));
                            return failed;
                        });

                log.info("Submit " + chunk.size() + " users");

                return chunkFuture.get();
            }
        }.call();
    }
}
