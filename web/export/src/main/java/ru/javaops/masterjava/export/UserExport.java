package ru.javaops.masterjava.export;

import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * gkislin
 * 14.10.2016
 */
public class UserExport {

    public List<User> process(final InputStream is) throws XMLStreamException {
        final StaxStreamProcessor processor = new StaxStreamProcessor(is);
        List<User> users = new ArrayList<>();

        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            final String email = processor.getAttribute("email");
            final UserFlag flag = UserFlag.valueOf(processor.getAttribute("flag"));
            final String fullName = processor.getReader().getElementText();
            final User user = new User(fullName, email, flag);
            users.add(user);
        }
        return users;
    }

    public List<User> addToDB(List<User> users, int chunk) {
        UserDao userDao = DBIProvider.getDao(UserDao.class);
        int[] result = userDao.insertBatch(users, chunk);
        List<User> missingUsers = new ArrayList<>();
        if (result.length != users.size()) {
            missingUsers = new ArrayList<>(users);
            for (int id: result) {
                User user = userDao.getWithId(id);
                missingUsers.removeIf(u -> u.getEmail().equals(user.getEmail()));
            }

        }
        return missingUsers;
    }

}
