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
import java.util.Map;

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
            while(!"".equals(line)) {
                log.debug("{}", line);
                line = br.readLine();
                if(line.startsWith("Content-Length")) {
                    contentLength = getContentLength(line);
                }
            }

            if(url.startsWith("/user/create")) {
                String body = IOUtils.readData(br, contentLength);
                Map<String, String> userMap = HttpRequestUtils.parseQueryString(body);
                User user = new User(userMap.get("userId"), userMap.get("password"), userMap.get("name"), userMap.get("email"));

                log.debug("User : {}", user);
            } else {
                response200(out, url);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
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
}
