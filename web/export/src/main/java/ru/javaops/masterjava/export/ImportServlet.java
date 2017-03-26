package ru.javaops.masterjava.export;

import org.thymeleaf.context.WebContext;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static ru.javaops.masterjava.export.ThymeleafListener.engine;

@WebServlet(urlPatterns = "/result")
public class ImportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale());
        UserDao userDao = DBIProvider.getDao(UserDao.class);
        List<User> users = userDao.getAll();
        webContext.setVariable("users", users);
        engine.process("result", webContext, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale());
        UserDao userDao = DBIProvider.getDao(UserDao.class);
        int limit = Integer.parseInt(req.getParameter("limit"));
        List<User> users = userDao.getWithLimit(limit);
        webContext.setVariable("users", users);
        engine.process("result", webContext, resp.getWriter());
    }
}
