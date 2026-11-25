package com.connectify.servlet;

import com.connectify.dao.FriendDAO;
import com.connectify.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * POST /friend — send / accept / reject a friend request.
 * action=send (receiverId) | action=accept (requestId) | action=reject (requestId)
 * GET /friend — show friends + pending requests + suggestions (friends.html).
 */
public class FriendServlet extends HttpServlet {
    private final FriendDAO friendDAO = new FriendDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/index.html"); return; }
        req.setAttribute("friends", friendDAO.getFriends(user.getUserId()));
        req.setAttribute("pendingRequests", friendDAO.getPendingRequests(user.getUserId()));
        req.setAttribute("suggestedFriends", friendDAO.getSuggestedFriends(user.getUserId()));
        req.getRequestDispatcher("/friends.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) { resp.sendRedirect(req.getContextPath() + "/index.html"); return; }

        String action = req.getParameter("action");
        if ("send".equals(action)) {
            friendDAO.sendRequest(user.getUserId(), Integer.parseInt(req.getParameter("receiverId")));
        } else if ("accept".equals(action)) {
            friendDAO.acceptRequest(Integer.parseInt(req.getParameter("requestId"))); // transactional
        } else if ("reject".equals(action)) {
            friendDAO.rejectRequest(Integer.parseInt(req.getParameter("requestId")));
        }
        resp.sendRedirect(req.getContextPath() + "/friend");
    }
}
