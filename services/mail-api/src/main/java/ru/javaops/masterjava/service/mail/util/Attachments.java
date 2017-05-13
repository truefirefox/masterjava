package ru.javaops.masterjava.service.mail.util;

import org.apache.commons.io.input.CloseShieldInputStream;
import ru.javaops.masterjava.service.mail.Attach;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Attachments {
    public static Attach getAttach(String name, InputStream inputStream) throws IOException {
        return new Attach(name, new DataHandler(new InputStreamDataSource(inputStream)));
    }

    //    http://stackoverflow.com/questions/2830561/how-to-convert-an-inputstream-to-a-datahandler
    //    http://stackoverflow.com/a/5924019/548473

    private static class InputStreamDataSource implements DataSource {
        private BufferedInputStream bufferedInputStream;

        InputStreamDataSource(InputStream inputStream) throws IOException {
            this.bufferedInputStream = new BufferedInputStream(inputStream);
            this.bufferedInputStream.mark(inputStream.available()+1);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            bufferedInputStream.reset();
            return new CloseShieldInputStream(bufferedInputStream);
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getContentType() {
            return "application/octet-stream";
        }

        @Override
        public String getName() {
            return "";
        }
    }
}
