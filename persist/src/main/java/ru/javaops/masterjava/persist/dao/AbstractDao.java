package ru.javaops.masterjava.persist.dao;

import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.Transaction;
import ru.javaops.masterjava.persist.DBIProvider;

/**
 * gkislin
 * 27.10.2016
 * <p>
 * <p>
 */
public interface AbstractDao {
    void clean();

    @SqlQuery("SELECT nextval('global_seq')")
    int getNextVal();

    @Transaction
    default int getSeqAndSkip(int step) {
        int id = getNextVal();
        DBIProvider.getDBI().useHandle(h -> h.execute("ALTER SEQUENCE global_seq RESTART WITH " + (id + step)));
        return id;
    }
}
