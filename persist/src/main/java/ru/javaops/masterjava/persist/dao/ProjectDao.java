package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.Project;

/**
 * Created by val on 2017-04-06.
 */
@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class ProjectDao implements AbstractDao{

    @SqlQuery("INSERT INTO projects (id, description, name) VALUES (:id, :description, :name)" +
            "ON CONFLICT ON CONSTRAINT name_unique DO UPDATE SET name=EXCLUDED.name RETURNING id")
    public abstract int insertWitId(@BindBean Project project);

    @SqlUpdate("TRUNCATE projects")
    @Override
    public abstract void clean();
}
