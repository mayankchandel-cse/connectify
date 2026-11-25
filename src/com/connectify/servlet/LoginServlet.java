package com.connectify.servlet;

import com.connectify.model.User;
import com.connectify.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** POST /login — check credentials, store the user in the session, go to the feed. */
public class LoginServlet extends HttpServlet {
    private final AuthService auth = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/index.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        User user = auth.login(email, password);
        if (user != null) {
            HttpSession session = req.getSession();
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            resp.sendRedirect(req.getContextPath() + "/feed");
        } else {
            req.setAttribute("error", "Invalid email or password. Try rahul@gmail.com / rahul123");
            req.getRequestDispatcher("/index.html").forward(req, resp);
        }
    }
}
