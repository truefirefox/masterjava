package ru.javaops.masterjava.persist.dao;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.UserTestData;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.javaops.masterjava.persist.UserTestData.*;

/**
 * gkislin
 * 27.10.2016
 */
public class UserDaoTest extends AbstractDaoTest<UserDao> {

    public UserDaoTest() {
        super(UserDao.class);
    }

    @BeforeClass
    public static void init() throws Exception {
        UserTestData.init();
    }

    @Before
    public void setUp() throws Exception {
        UserTestData.setUp();
    }

    @Test
    public void getWithLimit() {
        List<User> users = dao.getWithLimit(5);
        Assert.assertEquals(FIST5_USERS, users);
    }

    @Test
    public void insert() {
        User user = new User("UserForInsert", "userForInsert@gmail.com", UserFlag.active);
        dao.insert(user);

    }

    @Test
    public void withBatch() {
        List<User> users = ImmutableList.of(USER1,USER5,USER6,USER3,USER2);
        List<Integer> result = IntStream.of(
                dao.insertBatch(users,2))
                .boxed()
                .collect(Collectors.toList());

        //result.forEach(System.out::println);

        List<User> missingUsers = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            if (result.get(i) == 0) {
                missingUsers.add(users.get(i));
            }
        }
        Assert.assertArrayEquals(missingUsers.toArray(), new User[] {USER1, USER3, USER2});
    }
}