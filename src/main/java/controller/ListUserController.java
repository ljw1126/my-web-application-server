package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import http.HttpRequest;
import http.HttpResponse;

import java.util.Collection;
import java.util.Map;

public class ListUserController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(ListUserController.class);
    @Override
    protected void doGet(HttpRequest request, HttpResponse response) {
        if(!isLogin(request.getHeader("Cookie"))) {
            response.sendRedirect("/index.html");
            return;
        }

        Collection<User> users = DataBase.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");
        users.forEach(user -> {
            sb.append("<tr><td>" + user.getUserId() + "</td>");
            sb.append("<td>" + user.getName() + "</td>");
            sb.append("<td>" + user.getEmail() + "</td></tr>");
        });
        sb.append("</table>");

        response.forwardBody(sb.toString());
    }

    private boolean isLogin(String cookieValue) {
        Map<String, String> cookieMap = HttpRequestUtils.parseCookies(cookieValue);
        String value = cookieMap.get("logined");
        if(value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }
}
