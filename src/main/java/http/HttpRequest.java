package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private RequestLine requestLine;
    private HttpHeaders headers;
    private HttpParams requestParams = new HttpParams();

    public HttpRequest(InputStream inputStream) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            requestLine = new RequestLine(createRequestLine(br));
            requestParams.addQueryString(requestLine.getQueryString());
            headers = new HttpHeaders(br);
            requestParams.addBody(IOUtils.readData(br, headers.getContentLength()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String createRequestLine(BufferedReader br) throws IOException {
        String line = br.readLine();
        if(line == null) {
            throw new IllegalArgumentException();
        }

        return line;
    }

    public HttpMethod getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeader(String key) {
        return headers.getHeader(key);
    }

    public String getParameter(String key) {
        return requestParams.getParameter(key);
    }

    public HttpCookie getCookies() {
        return headers.getCookies();
    }

    public HttpSession getSession() {
        return headers.getSession();
    }
}
