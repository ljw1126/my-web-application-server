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
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = br.readLine();
            log.debug("first line : {}", line);

            if(line == null) {
                return;
            }

            String[] tokens = line.split(" ");

            String url = tokens[1];
            int contentLength = 0;
            String cookie = "";
            while(!"".equals(line)) {
                log.debug("{}", line);
                line = br.readLine();
                if(line.startsWith("Content-Length")) {
                    contentLength = getContentLength(line);
                }

                if(line.startsWith("Cookie")) {
                    cookie = line;
                }
            }

            DataOutputStream dos = new DataOutputStream(out);
            if(url.startsWith("/user/create")) {
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> userMap = HttpRequestUtils.parseQueryString(body);

                User user = new User(userMap.get("userId"), userMap.get("password"), userMap.get("name"), userMap.get("email"));
                DataBase.addUser(user);

                log.debug("User : {}", user);
                response302Header(dos);
            } else if(url.startsWith("/user/login")) {
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> userMap = HttpRequestUtils.parseQueryString(body);

                User user = DataBase.findUserById(userMap.get("userId"));
                if (user == null || !user.getPassword().equals(userMap.get("password"))) {
                    responseLoginFail(dos);
                } else {
                    responseLoginSuccessHeader(dos);
                    response200(dos, "/index.html"); //TODO 코드값
                }
            } else if(url.startsWith("/user/list")) {
                log.debug("String Cookie : {}", cookie);
                int idx = cookie.indexOf(" ");
                Map<String, String> cookieMap = HttpRequestUtils.parseCookies(cookie.substring(idx + 1));
                boolean logined = Boolean.parseBoolean(cookieMap.get("logined"));
                if(logined) {
                    StringBuilder sb = new StringBuilder();
                    List<User> userList = DataBase.findAll().stream().collect(Collectors.toList());
                    sb.append("<table>");
                    userList.forEach(user -> sb.append("<tr><td>" + user.getName() + "<td></tr>"));
                    sb.append("</table>");

                    String body = sb.toString();
                    response200Header(dos, body.length());
                    responseBody(dos, body.getBytes());
                } else {
                    response200(dos, "/index.html"); //TODO 코드값
                }
            } else {
                response200(dos, url);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200(DataOutputStream dos, String url) throws IOException {
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
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

    private int getContentLength(String line) {
        String[] tokens = line.split(":");
        return Integer.parseInt(tokens[1].trim());
    }

    private void responseLoginSuccessHeader(DataOutputStream dos) {
        try{
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Set-Cookie: logined=true");
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
