package ru.javaops.masterjava.service.mail.rest;


import com.google.common.collect.ImmutableList;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.validator.constraints.NotBlank;
import ru.javaops.masterjava.service.mail.Attach;
import ru.javaops.masterjava.service.mail.GroupResult;
import ru.javaops.masterjava.service.mail.MailServiceExecutor;
import ru.javaops.masterjava.service.mail.MailWSClient;
import ru.javaops.masterjava.service.mail.util.Attachments;
import ru.javaops.web.WebStateException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Path("/")
public class MailRS {
    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "Test";
    }

    @POST
    @Path("send")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public GroupResult send(@NotBlank @FormDataParam("users") String users,
                            @FormDataParam("subject") String subject,
                            @NotBlank @FormDataParam("body") String body,
                            @FormDataParam("attach") InputStream uploadedInputStream,
                            @FormDataParam("attach") FormDataContentDisposition fileDetail) throws WebStateException {

        List<Attach> attaches;
        if (uploadedInputStream == null) {
            attaches = ImmutableList.of();
        } else {
            String name = fileDetail.getFileName();
            try {
                name = new String(fileDetail.getFileName().getBytes ("iso-8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            attaches = ImmutableList.of(Attachments.getAttach(name, uploadedInputStream));
        }
        return MailServiceExecutor.sendBulk(MailWSClient.split(users), subject, body, attaches);
    }
}