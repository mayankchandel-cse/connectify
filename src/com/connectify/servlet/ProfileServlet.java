package com.connectify.servlet;

import com.connectify.dao.PostDAO;
import com.connectify.dao.UserDAO;
import com.connectify.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * GET  /profile — show a user's info + their posts (profile.html).
 *      ?userId=2 views someone else; no param views yourself.
 * POST /profile — update your own profile (name, bio, dob, gender).
 */
public class ProfileServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final PostDAO postDAO = new PostDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User me = session == null ? null : (User) session.getAttribute("user");
        if (me == null) { resp.sendRedirect(req.getContextPath() + "/index.html"); return; }

        int userId = me.getUserId();
        String param = req.getParameter("userId");
        if (param != null && !param.isEmpty()) userId = Integer.parseInt(param);

        User profileUser = userDAO.getUserById(userId);
        if (profileUser == null) { resp.sendRedirect(req.getContextPath() + "/feed"); return; }
        req.setAttribute("profileUser", profileUser);
        req.setAttribute("userPosts", postDAO.getPostsByUser(userId));
        req.getRequestDispatcher("/profile.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User me = session == null ? null : (User) session.getAttribute("user");
        if (me == null) { resp.sendRedirect(req.getContextPath() + "/index.html"); return; }

        me.setName(req.getParameter("name"));
        me.setBio(req.getParameter("bio"));
        me.setDob(req.getParameter("dob"));
        me.setGender(req.getParameter("gender"));
        userDAO.updateProfile(me);
        session.setAttribute("user", me);
        resp.sendRedirect(req.getContextPath() + "/profile");
    }
}
