package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import one.util.streamex.IntStreamEx;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.model.User;

import java.util.*;

/**
 * gkislin
 * 27.10.2016
 * <p>
 * <p>
 */
//TODO update group_user table
@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class UserDao implements AbstractDao {

    private final GroupDao groupDao = DBIProvider.getDao(GroupDao.class);

    public User insert(User user) {
        if (user.isNew()) {
            int id = insertGeneratedId(user);
            user.setId(id);
        } else {
            insertWitId(user);
        }
        return user;
    }

    @SqlUpdate("INSERT INTO users (full_name, email, flag, city) VALUES (:fullName, :email, CAST(:flag AS USER_FLAG), :city) ")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean User user);

    @SqlUpdate("INSERT INTO users (id, full_name, email, flag, city) VALUES (:id, :fullName, :email, CAST(:flag AS USER_FLAG), :city) ")
    abstract void insertWitId(@BindBean User user);

    @SqlQuery("SELECT * FROM users ORDER BY full_name, email LIMIT :it")
    public abstract List<User> getWithLimit(@Bind int limit);

    //   http://stackoverflow.com/questions/13223820/postgresql-delete-all-content
    @SqlUpdate("TRUNCATE users")
    @Override
    public abstract void clean();

    //    https://habrahabr.ru/post/264281/
    @SqlBatch("INSERT INTO users (id, full_name, email, flag, city) VALUES (:id, :fullName, :email, CAST(:flag AS USER_FLAG), :city)" +
            "ON CONFLICT DO NOTHING")
//            "ON CONFLICT (email) DO UPDATE SET full_name=:fullName, flag=CAST(:flag AS USER_FLAG)")
    public abstract int[] insertBatch(@BindBean Collection<User> users, @BatchChunkSize int chunkSize);


    public List<User> insertAndGetConflicts(List<User> users) {
        int[] result = insertBatch(users, users.size());
        return IntStreamEx.range(0, users.size())
                .filter(i -> result[i] == 0)
                .mapToObj(users::get)
                .toList();
    }



}
