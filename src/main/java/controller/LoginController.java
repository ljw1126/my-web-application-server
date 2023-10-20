package controller;

import db.DataBase;
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
            response.addHeader("Set-Cookie", "logined=false");
            response.forward("/user/login_failed.html");
        } else {
            response.addHeader("Set-Cookie", "logined=true");
            response.sendRedirect("/index.html");
        }
    }
}
