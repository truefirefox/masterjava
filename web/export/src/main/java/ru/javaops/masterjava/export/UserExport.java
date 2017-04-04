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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        //result of SqlBatch int[] only (-> List<Integer>)
        List<Integer> result = IntStream.of(userDao.insertBatch(users, chunk))
                .boxed()
                .collect(Collectors.toList());

        //find missing users
        List<User> missingUsers = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            if (result.get(i) == 0) {
                missingUsers.add(users.get(i));
            }
        }
        return missingUsers;
    }

}
