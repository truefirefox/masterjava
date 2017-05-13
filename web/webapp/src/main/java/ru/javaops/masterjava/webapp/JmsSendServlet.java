package ru.javaops.masterjava.webapp;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.service.mail.MailData;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/sendJms")
@Slf4j
@MultipartConfig
public class JmsSendServlet extends HttpServlet {
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            InitialContext initCtx = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) initCtx.lookup("java:comp/env/jms/ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer((Destination) initCtx.lookup("java:comp/env/jms/queue/MailQueue"));
        } catch (Exception e) {
            throw new IllegalStateException("JMS init failed", e);
        }
    }

    @Override
    public void destroy() {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException ex) {
                log.warn("Couldn't close JMSConnection: ", ex);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String users = req.getParameter("users");
        String subject = req.getParameter("subject");
        String body = req.getParameter("body");

        Map<String, byte []> attaches;
        Part filePart = req.getPart("attach");
        if (filePart.getSize() == 0) {
            attaches = ImmutableMap.of();
        } else {
            byte[] toAttach = new byte[filePart.getInputStream().available()];
            filePart.getInputStream().read(toAttach);
            attaches = ImmutableMap.of(filePart.getSubmittedFileName(), toAttach);

        }
        resp.getWriter().write(sendJms(users, subject, body, attaches));
    }

    private synchronized String sendJms(String users, String subject, String body, Map<String, byte[]> attaches) {
        String msg;
        try {
            HashMap<String, byte[]> attachMap = new HashMap<>(attaches);
            MailData mailData = new MailData(users, subject, body, attachMap);
            ObjectMessage om = session.createObjectMessage();
            om.setObject(mailData);
            producer.send(om);
            msg = "Successfully sent message.";
            log.info(msg);
        } catch (Exception e) {
            msg = "Sending JMS message failed: " + e.getMessage();
            log.error(msg, e);
        }
        return msg;
    }
}