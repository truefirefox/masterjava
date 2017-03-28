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

        //list of rowNumbers
        List<Integer> numbers = IntStream.range(0, users.size()).boxed().collect(Collectors.toList());

        //result of SqlBatch int[] only (-> List<Integer>)
        List<Integer> result = IntStream.of(userDao.insertBatch(numbers, users, chunk))
                .boxed()
                .collect(Collectors.toList());

        //find missing users
        return users.stream()
                .filter(u -> !result.contains(users.indexOf(u)))
                .collect(Collectors.toList());
    }

}
