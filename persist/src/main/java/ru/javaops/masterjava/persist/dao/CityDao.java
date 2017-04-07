package ru.javaops.masterjava.persist.dao;

import one.util.streamex.IntStreamEx;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

/**
 * Created by val on 2017-04-06.
 */
public abstract class CityDao implements AbstractDao {

    public City insert(City city) {
        if (city.isNew()) {
            int id = insertGeneratedId(city);
            city.setId(id);
        } else {
            insertWitId(city);
        }
        return city;
    }

    @SqlQuery("SELECT nextval('global_seq')")
    abstract int getNextVal();

    @Transaction
    public int getSeqAndSkip(int step) {
        int id = getNextVal();
        DBIProvider.getDBI().useHandle(h -> h.execute("ALTER SEQUENCE global_seq RESTART WITH " + (id + step)));
        return id;
    }

    @SqlUpdate("INSERT INTO cities (value, value_id) VALUES (:value, :valueId)")
    @GetGeneratedKeys
    abstract int insertGeneratedId(@BindBean City city);

    @SqlUpdate("INSERT INTO cities (id, value, value_id) VALUES (:id, :value, :valueId)")
    abstract void insertWitId(@BindBean City city);

    @SqlBatch("INSERT INTO cities (id, value, value_id) VALUES (:id, :value, :valueId)" +
            "ON CONFLICT DO NOTHING")
    public abstract int[] insertBatch(@BindBean List<City> cities, @BatchChunkSize int chunkSize);

    public List<String> insertAndGetConflictValueId(List<City> cities) {
        int[] result = insertBatch(cities, cities.size());
        return IntStreamEx.range(0, cities.size())
                .filter(i -> result[i] == 0)
                .mapToObj(index -> cities.get(index).getValueId())
                .toList();
    }

    @SqlUpdate("TRUNCATE cities")
    @Override
    public abstract void clean();
}
