package ru.javaops.masterjava.service.mail;

import com.google.common.collect.ImmutableSet;
import ru.javaops.web.WebStateException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class MailServiceClient {

    public static void main(String[] args) throws MalformedURLException {
        Service service = Service.create(
                new URL("http://localhost:8080/mail/mailService?wsdl"),
                new QName("http://mail.javaops.ru/", "MailServiceImplService"));

        MailService mailService = service.getPort(MailService.class);

        File file = new File("C:\\Users\\val\\Documents\\scooter.pdf");//File.createTempFile("somefilename-", null, null);
        DataHandler attachment = new DataHandler(new FileDataSource(file));

        ImmutableSet<Addressee> addressees = ImmutableSet.of(
                new Addressee("<ptv0609@gmail.com>")//,
                //new Addressee("Мастер Java <masterjava@javaops.ru>"),
                //new Addressee("Bad Email <bad_email.ru>")
                 );

        try {
            String status = mailService.sendToGroup(addressees, ImmutableSet.of(), "Bulk email subject", "Bulk email body", attachment, file.getName());
            System.out.println(status);

            GroupResult groupResult = mailService.sendBulk(addressees, "Individual mail subject", "Individual mail body", attachment, file.getName());
            System.out.println(groupResult);
        } catch (WebStateException e) {
            System.out.println(e);
        }
    }
}
