package com.connectify.servlet;

import com.connectify.service.AuthService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** POST /register — validate input, create the account, redirect to login. */
public class RegisterServlet extends HttpServlet {
    private final AuthService auth = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String error = auth.register(
                req.getParameter("name"),
                req.getParameter("email"),
                req.getParameter("password"),
                req.getParameter("confirmPassword"),
                req.getParameter("bio"),
                req.getParameter("dob"),
                req.getParameter("gender"));
        if (error == null) {
            req.setAttribute("message", "Account created! Please log in.");
            req.getRequestDispatcher("/index.html").forward(req, resp);
        } else {
            req.setAttribute("error", error);
            req.getRequestDispatcher("/index.html").forward(req, resp);
        }
    }
}
