package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import one.util.streamex.IntStreamEx;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.model.Group;

import java.util.List;

/**
 * Created by val on 2017-04-06.
 */
@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class GroupDao implements AbstractDao{

    public Group insert(Group group) {
        if (group.isNew()) {
            int id = insertGeneratedId(group);
            group.setId(id);
        } else {
            insertWitId(group);
        }
        return group;
    }

    @SqlQuery("SELECT nextval('global_seq')")
    abstract int getNextVal();

    @Transaction
    public int getSeqAndSkip(int step) {
        int id = getNextVal();
        DBIProvider.getDBI().useHandle(h -> h.execute("ALTER SEQUENCE global_seq RESTART WITH " + (id + step)));
        return id;
    }

    @SqlUpdate("INSERT INTO groups (name, type, project_id) VALUES (:name, CAST(:type AS group_type), :projectId)")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean Group group);

    @SqlUpdate("INSERT INTO groups (id, name, type, project_id) VALUES (:id, :name, CAST(:type AS group_type), :projectId)")
    abstract void insertWitId(@BindBean Group group);

    @SqlBatch("INSERT INTO groups (id, name, type, project_id) VALUES (:id, :name, CAST(:type AS group_type), :projectId)" +
            "ON CONFLICT DO NOTHING")
    public abstract int[] insertBatch(@BindBean List<Group> groups, @BatchChunkSize int chunkSize);

    public List<String> insertAndGetConflictName(List<Group> groups) {
        int[] result = insertBatch(groups, groups.size());
        return IntStreamEx.range(0, groups.size())
                .filter(i -> result[i] == 0)
                .mapToObj(index -> groups.get(index).getName())
                .toList();
    }

    @SqlUpdate("TRUNCATE projects")
    @Override
    public abstract void clean();
}
