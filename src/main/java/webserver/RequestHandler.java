package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            String path = request.getPath();

            if(path.startsWith("/user/create") && !path.endsWith(".html")) {
                User user = new User(request.getParameter("userId"),
                        request.getParameter("password"),
                        request.getParameter("name"),
                        request.getParameter("email"));
                log.debug("User : {}", user);

                DataBase.addUser(user);

                response.sendRedirect("/index.html");
            } else if(path.startsWith("/user/login") && !path.endsWith(".html")) {
                User user = DataBase.findUserById(request.getParameter("userId"));
                if (user == null || !user.getPassword().equals(request.getParameter("password"))) {
                    response.addHeader("Set-Cookie", "logined=false");
                    response.forward("/user/login_failed.html");
                } else {
                    response.addHeader("Set-Cookie", "logined=true");
                    response.sendRedirect("/index.html");
                }
            } else if(path.startsWith("/user/list") && !path.endsWith(".html")) {
                if (isLogin(request.getHeader("Cookie"))) {
                    Collection<User> userList = DataBase.findAll();

                    StringBuilder sb = new StringBuilder();
                    sb.append("<table border='1'>");
                    userList.forEach(user -> {
                        sb.append("<tr><td>" + user.getUserId() + "</td>");
                        sb.append("<td>" + user.getName() + "</td>");
                        sb.append("<td>" + user.getEmail() + "</td></tr>");
                    });
                    sb.append("</table>");

                    response.forwardBody(sb.toString());
                } else {
                    response.sendRedirect("/index.html");
                }
            } else {
                response.forward(path);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
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
