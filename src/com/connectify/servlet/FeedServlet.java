package com.connectify.servlet;

import com.connectify.dao.CommentDAO;
import com.connectify.dao.LikeDAO;
import com.connectify.model.Comment;
import com.connectify.model.Post;
import com.connectify.model.User;
import com.connectify.service.FeedService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GET /feed — load the news feed (ArrayList<Post>), like counts (HashMap),
 * comments per post, and suggested friends; forward to feed.html data via attributes.
 * In a full JSP setup this forwards to feed.jsp; here it forwards to feed.html
 * with request attributes that a JSP/templating layer would render.
 */
public class FeedServlet extends HttpServlet {
    private final FeedService feedService = new FeedService();
    private final CommentDAO commentDAO = new CommentDAO();
    private final LikeDAO likeDAO = new LikeDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/index.html"); return; }

        List<Post> feed = feedService.loadFeed(user.getUserId());
        String sort = req.getParameter("sort");
        if ("likes".equals(sort)) feedService.sortPostsByLikes(feed);
        else feedService.sortPostsByDate(feed);

        Map<Integer, List<Comment>> commentsByPost = new HashMap<>();
        Map<Integer, Boolean> likedByMe = new HashMap<>();
        for (Post p : feed) {
            commentsByPost.put(p.getPostId(), commentDAO.getCommentsByPost(p.getPostId()));
            likedByMe.put(p.getPostId(), likeDAO.isLiked(user.getUserId(), p.getPostId()));
        }
        req.setAttribute("feed", feed);
        req.setAttribute("commentsByPost", commentsByPost);
        req.setAttribute("likedByMe", likedByMe);
        req.setAttribute("notifications", feedService.getNotifications());
        req.getRequestDispatcher("/feed.html").forward(req, resp);
    }
}
