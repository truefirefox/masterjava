package ru.javaops.masterjava.export;

import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by val on 2017-04-06.
 */
@Slf4j
public class PayloadExport {

    private static final int NUMBER_THREADS = 4;
    private final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);

    private final ProjectGroupExport projectGroupExport = new ProjectGroupExport();
    private final CityExport cityExport = new CityExport();
    private final UserExport userExport = new UserExport();

    public List<String> process (final InputStream is, int chunkSize) throws XMLStreamException {
        log.info("Start processing with chunkSize=" + chunkSize);

        final StaxStreamProcessor processor = new StaxStreamProcessor(is);
        List<String> result = new ArrayList<>();

        try {
            result.addAll(projectGroupExport.process(executorService, processor, chunkSize));
            result.addAll(cityExport.process(executorService, processor, chunkSize));
            result.addAll(userExport.process(executorService, processor, chunkSize));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
