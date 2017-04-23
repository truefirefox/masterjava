package ru.javaops.masterjava.webapp;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import org.thymeleaf.context.WebContext;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.service.mail.Addressee;
import ru.javaops.masterjava.service.mail.MailWSClient;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.javaops.masterjava.common.web.ThymeleafListener.engine;

@WebServlet("")
public class UsersServlet extends HttpServlet {
    private UserDao userDao = DBIProvider.getDao(UserDao.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale(),
                ImmutableMap.of("users", userDao.getWithLimit(20)));
        engine.process("users", webContext, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //super.doPost(req, resp);
        //List<Integer> ids = new ArrayList<>();
        List<Integer> ids = req.getReader().lines()
                .map(line -> Splitter.on(',').splitToList(line))
                .flatMap(list -> list.stream().map(Integer::valueOf))
                .collect(Collectors.toList());
        List<User> users = userDao.getByIds(ids);
        Set<Addressee> to = users.stream().map(u -> new Addressee(u.getEmail(), u.getFullName())).collect(Collectors.toSet());
        MailWSClient.sendMail(to, to, "testSubject", "testBody");
    }
}
