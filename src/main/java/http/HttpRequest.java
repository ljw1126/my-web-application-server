package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private static Map<String, String> header = new HashMap<>();
    private static Map<String, String> parameter = new HashMap<>();

    private RequestLine requestLine;

    public HttpRequest(InputStream inputStream) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = br.readLine();
            if (line == null) return;

            requestLine = new RequestLine(line);

            while (!"".equals(line = br.readLine())) {
                if (line == null) break;

                String[] headerTokens = line.split(":");
                header.put(headerTokens[0].trim(), headerTokens[1].trim());
            }

            if (getMethod().isPost()) {
                String body = IOUtils.readData(br, Integer.parseInt(getHeader("Content-Length")));
                parameter = HttpRequestUtils.parseQueryString(body);
            } else {
                parameter = requestLine.getParams();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public HttpMethod getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeader(String key) {
        return header.get(key);
    }

    public String getParameter(String key) {
        return parameter.get(key);
    }

    public HttpCookie getCookies() {
        return new HttpCookie(getHeader("Cookie"));
    }

    public HttpSession getSession() {
        return HttpSessions.getSession(getCookies().getCookie("JSESSIONID"));
    }
}
