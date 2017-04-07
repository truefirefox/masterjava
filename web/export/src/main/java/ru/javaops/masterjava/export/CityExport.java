package ru.javaops.masterjava.export;

import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.CityDao;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by val on 2017-04-06.
 */
@Slf4j
public class CityExport {

    private static final int NUMBER_THREADS = 4;
    private final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);
    private final CityDao cityDao = DBIProvider.getDao(CityDao.class);

    public List<String> process(final StaxStreamProcessor processor, int chunkSize) throws Exception {

        return new Callable<List<String>>() {

            @Override
            public List<String> call() throws Exception {

                List<String> futures = new ArrayList<>();
                int id = cityDao.getSeqAndSkip(chunkSize);
                List<City> chunk = new ArrayList<>(chunkSize);

                // Cities loop
                String tag;
                while ((tag = processor.doUntilAny(XMLEvent.START_ELEMENT, "City", "Users")) != null) {

                    if (tag.equals("Users")) break;

                    final String valueId = processor.getAttribute("id");
                    final String value = processor.getReader().getElementText();

                    final City city = new City(id++, value, valueId);
                    chunk.add(city);

                    if (chunk.size() == chunkSize) {
                        futures.addAll(submit(chunk));
                        chunk = new ArrayList<>(chunkSize);
                        id = cityDao.getSeqAndSkip(chunkSize);
                    }
                }
                if (!chunk.isEmpty())
                    futures.addAll(submit(chunk));
                return futures;
            }

            private List<String> submit(List<City> cities) throws Exception {
                Future<List<String>> chunkFuture = executorService.submit(() -> {
                    List<String> stringsResult = new ArrayList<>();
                    cityDao.insertAndGetConflictValueId(cities)
                            .forEach(s -> stringsResult.add("city with id " + s + " already present"));
                    return stringsResult;
                });
                log.info("Submit " + cities.size() + " cities");
                //return List<String> with fail messages
                return chunkFuture.get();
            }
        }.call();
    }
}
