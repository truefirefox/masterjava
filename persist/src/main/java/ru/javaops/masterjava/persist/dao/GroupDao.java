package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import one.util.streamex.IntStreamEx;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.Group;

import java.util.List;

/**
 * Created by val on 2017-04-06.
 */
@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class GroupDao implements AbstractDao{

    @SqlBatch("INSERT INTO groups (id, name, type, project_id) VALUES (:id, :name, CAST(:type AS group_type), :projectId)" +
            "ON CONFLICT DO NOTHING")
    public abstract int[] insertBatch(@BindBean List<Group> groups, @BatchChunkSize int chunkSize);

    @SqlBatch("INSERT INTO user_group (user_email, group_name) VALUES (:userEmail, :name)" +
            "ON CONFLICT DO NOTHING")
    public abstract int[] updateGroups(@Bind("userEmail") String email, @Bind("name") List<String> groups);

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
