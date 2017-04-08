package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.UserTestData;

import static ru.javaops.masterjava.persist.UserTestData.FIST5_USERS;

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

    /*@Before
    public void setUp() throws Exception {
        UserTestData.setUp();
    }

    @Test
    public void getWithLimit() {
        List<User> users = dao.getWithLimit(5);
        Assert.assertEquals(FIST5_USERS, users);
    }

    @Test
    public void insertBatch() throws Exception {
        dao.clean();
        dao.insertBatch(FIST5_USERS, 3);
        Assert.assertEquals(5, dao.getWithLimit(100).size());
    }*/

    @Test
    public void getSeqAndSkip() throws Exception {
        int seq1 = dao.getSeqAndSkip(5);
        int seq2 = dao.getSeqAndSkip(1);
        Assert.assertEquals(5, seq2 - seq1);
    }

    @Test
    public void insertBatchWithCity() throws Exception {
        for (int x: dao.insertBatchWithCity(FIST5_USERS, 3)) {
            System.out.println(x);
        }

        Assert.assertEquals(3, dao.getWithLimit(100).size());
    }
}