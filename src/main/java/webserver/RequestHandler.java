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
            String path = request.getPath();

            DataOutputStream dos = new DataOutputStream(out);
            if(path.startsWith("/user/create") && !path.endsWith(".html")) {
                User user = new User(request.getParameter("userId"),
                        request.getParameter("password"),
                        request.getParameter("name"),
                        request.getParameter("email"));
                DataBase.addUser(user);

                log.debug("User : {}", user);
                response302Header(dos);
            } else if(path.startsWith("/user/login") && !path.endsWith(".html")) {
                User user = DataBase.findUserById(request.getParameter("userId"));
                if (user == null || !user.getPassword().equals(request.getParameter("password"))) {
                    responseLoginFail(dos);
                } else {
                    response302HeaderWithCookie(dos, "logined=true");
                }
            } else if(path.startsWith("/user/list") && !path.endsWith(".html")) {
                if (isLogin(request.getHeader("Cookie"))) {
                    StringBuilder sb = new StringBuilder();
                    List<User> userList = DataBase.findAll().stream().collect(Collectors.toList());
                    sb.append("<table>");
                    userList.forEach(user -> sb.append("<tr><td>" + user.getName() + "<td></tr>"));
                    sb.append("</table>");

                    String body = sb.toString();
                    response200Header(dos, body.length());
                    responseBody(dos, body.getBytes());
                } else {
                    response302Header(dos);
                }
            } else if(path.endsWith(".css")) {
                log.debug("request css : {}", path);
                byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
                response200HeaderWithCss(dos, body.length);
                responseBody(dos, body);
            } else {
                byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
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

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200HeaderWithCss(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: /index.html\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302HeaderWithCookie(DataOutputStream dos, String cookie) {
        try{
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            dos.writeBytes("\r\n");
        }catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseLoginFail(DataOutputStream dos) throws IOException {
        byte[] body = Files.readAllBytes(new File("./webapp/user/login_failed.html").toPath());
        response401Header(dos);
        responseBody(dos, body);
    }
    private void response401Header(DataOutputStream dos) {
        try{
            dos.writeBytes("HTTP/1.1 401 Unauthorized\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Set-Cookie: logined=false");
            dos.writeBytes("\r\n");
        }catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
