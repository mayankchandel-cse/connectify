package com.connectify.servlet;

import com.connectify.dao.UserDAO;
import com.connectify.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** GET /search?q=rahul — search users by name or email (SQL LIKE), show results. */
public class SearchServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/index.html"); return; }

        String q = req.getParameter("q");
        if (q != null && !q.trim().isEmpty()) {
            req.setAttribute("results", userDAO.searchUsers(q.trim()));
            req.setAttribute("query", q.trim());
        }
        req.getRequestDispatcher("/friends.html").forward(req, resp);
    }
}
