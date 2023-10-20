package controller;

import db.DataBase;
import http.HttpSession;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import http.HttpRequest;
import http.HttpResponse;

public class LoginController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) {
        User user = DataBase.findUserById(request.getParameter("userId"));
        if(user == null || !user.getPassword().equals(request.getParameter("password"))) {
            response.forward("/user/login_failed.html");
        } else {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            response.sendRedirect("/index.html");
        }
    }
}
