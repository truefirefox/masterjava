package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import one.util.streamex.IntStreamEx;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.model.Project;

import java.util.List;

/**
 * Created by val on 2017-04-06.
 */
@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class ProjectDao implements AbstractDao{

    public Project insert(Project project) {
        if (project.isNew()) {
            int id = insertGeneratedId(project);
            project.setId(id);
        } else {
            insertWitId(project);
        }
        return project;
    }

    @SqlQuery("SELECT nextval('global_seq')")
    abstract int getNextVal();

    @Transaction
    public int getSeqAndSkip(int step) {
        int id = getNextVal();
        DBIProvider.getDBI().useHandle(h -> h.execute("ALTER SEQUENCE global_seq RESTART WITH " + (id + step)));
        return id;
    }

    @SqlUpdate("INSERT INTO projects (description, name) VALUES (:description, :name)")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean Project project);

    @SqlQuery("INSERT INTO projects (id, description, name) VALUES (:id, :description, :name)" +
            "ON CONFLICT ON CONSTRAINT name_unique DO UPDATE SET name=EXCLUDED.name RETURNING id")
    public abstract int insertWitId(@BindBean Project project);

    @SqlBatch("INSERT INTO projects (id, description, name) VALUES (:id, :description, :name)" +
            "ON CONFLICT DO NOTHING")
    public abstract int[] insertBatch(@BindBean List<Project> projects, @BatchChunkSize int chunkSize);

    public List<String> insertAndGetConflictName(List<Project> projects) {
        int[] result = insertBatch(projects, projects.size());
        return IntStreamEx.range(0, projects.size())
                .filter(i -> result[i] == 0)
                .mapToObj(index -> projects.get(index).getName())
                .toList();
    }

    @SqlUpdate("TRUNCATE projects")
    @Override
    public abstract void clean();
}
