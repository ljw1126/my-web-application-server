package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpHeaders {
    private static final Logger log = LoggerFactory.getLogger(HttpHeaders.class);
    private static final String COOKIE = "Cookie";
    private static final String CONTENT_LENGTH = "Content-Length";
    private Map<String, String> headers = new HashMap<>();
    public HttpHeaders(BufferedReader br) throws IOException {
        String line = br.readLine();
        while (!"".equals(line)) {
            if(line == null) break;

            add(line);
            line = br.readLine();
        }
    }

    public void add(String line) {
        log.debug("header : {}", line);

        String[] tokens = line.split(":");
        headers.put(tokens[0].trim(), tokens[1].trim());
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public int getIntHeader(String key) {
        String header = getHeader(key);
        return header == null ? 0 : Integer.parseInt(header);
    }

    public int getContentLength() {
        return getIntHeader(CONTENT_LENGTH);
    }

    public HttpCookie getCookies() {
        return new HttpCookie(getHeader(COOKIE));
    }

    public HttpSession getSession() {
        return HttpSessions.getSession(getCookies().getCookie("JSESSIONID"));
    }
}
