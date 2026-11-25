package com.connectify.servlet;

import com.connectify.dao.CommentDAO;
import com.connectify.model.Comment;
import com.connectify.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * POST /comment — add a comment (content, postId) or delete one (action=delete, commentId).
 */
public class CommentServlet extends HttpServlet {
    private final CommentDAO commentDAO = new CommentDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/index.html"); return; }

        if ("delete".equals(req.getParameter("action"))) {
            commentDAO.deleteComment(Integer.parseInt(req.getParameter("commentId")));
        } else {
            String content = req.getParameter("content");
            int postId = Integer.parseInt(req.getParameter("postId"));
            if (content != null && !content.trim().isEmpty()) {
                commentDAO.addComment(new Comment(user.getUserId(), postId, content.trim()));
            }
        }
        resp.sendRedirect(req.getContextPath() + "/feed");
    }
}
