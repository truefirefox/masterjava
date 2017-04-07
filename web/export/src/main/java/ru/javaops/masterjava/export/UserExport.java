package ru.javaops.masterjava.export;

import lombok.extern.slf4j.Slf4j;
import one.util.streamex.IntStreamEx;
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
                    final String[] groups = groupRefs != null ? groupRefs.split("\\s+") : new String[0];
                    final UserFlag flag = UserFlag.valueOf(processor.getAttribute("flag"));
                    final String fullName = processor.getReader().getElementText();
                    final User user = new User(id++, fullName, email, flag, city);
                    chunk.put(user, Arrays.asList(groups));
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
                List<String> failed = new ArrayList<>();
                Future<List<User>> chunkFuture = executorService.submit(
                        () -> userDao.insertAndGetConflicts(new ArrayList<>(chunk.keySet())));
                chunkFuture.get().forEach(u -> {
                    failed.add("User with email = " + u.getEmail() + " already present");
                    chunk.remove(u);
                });

                //actually we don't use it because we have "ON DELETE CASCADE ON UPDATE CASCADE" in user_group table
                chunk.forEach((User u, List<String> l) -> {
                    int[] result = groupDao.updateGroups(u.getEmail(), l);
                    IntStreamEx.range(0, l.size())
                            .filter(i -> result[i] == 0)
                            .forEach(s -> failed.add("failed to add group " + s + " for User " + u.getEmail()));
                });
                log.info("Submit " + chunk.size() + " users");
                return failed;
            }
        }.call();
    }
}
