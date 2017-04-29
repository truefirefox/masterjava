package ru.javaops.masterjava.webapp;

import com.sun.xml.ws.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.service.mail.MailWSClient;
import ru.javaops.web.WebStateException;

import javax.activation.DataHandler;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;

@MultipartConfig(maxFileSize = 2 * 1024 * 1024 * 1024L)
@WebServlet("/send")
@Slf4j
public class SendServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String users = req.getParameter("users");
        String subject = req.getParameter("subject");
        String body = req.getParameter("body");
        Part part = req.getPart("attachment");
        String attachmentName = "";
        DataHandler attachment = null;
        byte[] fileData = new byte[(int) part.getSize()];

        if (part.getSize() != 0) {
            part.getInputStream().read(fileData);
            attachmentName = part.getSubmittedFileName();
            attachment = new DataHandler(new ByteArrayDataSource(fileData, null));
        }

        String groupResult;
        try {
            groupResult = MailWSClient.sendBulk(MailWSClient.split(users), subject, body, attachment, attachmentName).toString();
        } catch (WebStateException e) {
            groupResult = e.toString();
        }
        resp.getWriter().write(groupResult);
    }
}
