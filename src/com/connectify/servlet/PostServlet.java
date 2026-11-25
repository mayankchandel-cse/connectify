package com.connectify.servlet;

import com.connectify.dao.PostDAO;
import com.connectify.model.Post;
import com.connectify.model.User;
import com.connectify.service.FeedService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * POST /post — create, edit or delete a post.
 * action=create (content) | action=edit (postId, content) | action=delete (postId)
 */
public class PostServlet extends HttpServlet {
    private final PostDAO postDAO = new PostDAO();
    private final FeedService feedService = new FeedService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/index.html"); return; }

        String action = req.getParameter("action");
        if ("create".equals(action)) {
            String content = req.getParameter("content");
            if (content != null && !content.trim().isEmpty()) {
                int id = postDAO.createPost(new Post(user.getUserId(), content.trim()));
                if (id > 0) feedService.recordAction("Created post #" + id);
            }
        } else if ("edit".equals(action)) {
            int postId = Integer.parseInt(req.getParameter("postId"));
            postDAO.updatePost(postId, req.getParameter("content"));
            feedService.recordAction("Edited post #" + postId);
        } else if ("delete".equals(action)) {
            int postId = Integer.parseInt(req.getParameter("postId"));
            postDAO.deletePost(postId);
            feedService.recordAction("Deleted post #" + postId);
        }
        resp.sendRedirect(req.getContextPath() + "/feed");
    }
}
