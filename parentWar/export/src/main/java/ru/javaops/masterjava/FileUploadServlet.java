package ru.javaops.masterjava;

import ru.javaops.masterjava.xml.schema.FlagType;
import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

//http://www.codejava.net/coding/upload-files-to-database-servlet-jsp-mysql#UploadServlet
@MultipartConfig(
        maxFileSize = 1024 * 1024 * 40,
        maxRequestSize = 1024 * 1024 *50,
        fileSizeThreshold = 1024 *1024 *3)
public class FileUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        Part filePart = request.getPart("uploadFile");

        if (filePart != null) {

            // obtains input stream of the upload file
            try (InputStream inputStream = filePart.getInputStream()) {

                request.setAttribute("users", getUsers(inputStream));
                getServletContext().getRequestDispatcher("/users.jsp").forward(request, response);

            } catch (XMLStreamException ex) {
                request.setAttribute("message", "There was an error: " + ex.getMessage());
                getServletContext().getRequestDispatcher("/message.jsp").forward(request, response);
            }
        }
    }

    private Set<User> getUsers(InputStream is) throws XMLStreamException {
        StaxStreamProcessor processor = new StaxStreamProcessor(is);
        // Users loop
        Set<User> users = new TreeSet<>(Comparator.comparing(User::getValue));
        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            User user = new User();
            user.setEmail(processor.getAttribute("email"));
            user.setFlag(FlagType.fromValue(processor.getAttribute("flag")));
            processor.doUntil(XMLEvent.START_ELEMENT, "name");
            user.setValue(processor.getText());
            users.add(user);
        }
        return users;
    }
}