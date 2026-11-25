package com.connectify.servlet;

import com.connectify.dao.LikeDAO;
import com.connectify.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** POST /like — toggle like / unlike for a post, then back to the feed. */
public class LikeServlet extends HttpServlet {
    private final LikeDAO likeDAO = new LikeDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/index.html"); return; }
        int postId = Integer.parseInt(req.getParameter("postId"));
        likeDAO.toggleLike(user.getUserId(), postId);
        resp.sendRedirect(req.getContextPath() + "/feed");
    }
}
