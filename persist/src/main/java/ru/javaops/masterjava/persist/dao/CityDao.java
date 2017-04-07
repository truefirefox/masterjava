package ru.javaops.masterjava.persist.dao;

import one.util.streamex.IntStreamEx;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

/**
 * Created by val on 2017-04-06.
 */
public abstract class CityDao implements AbstractDao {

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
