package webserver;

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
    Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private static Map<String, String> header = new HashMap<>();
    private static Map<String, String> parameter = new HashMap<>();

    private final HttpMethod httpMethod;
    private final String requestPath;

    public HttpRequest(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line = br.readLine();
        String[] tokens = line.split(" ");
        log.debug("tokens : {}", tokens);

        this.httpMethod = HttpMethod.valueOf(tokens[0]);

        while(!"".equals(line = br.readLine())) {
            if(line == null) break;

            String[] headerTokens = line.split(": ");
            header.put(headerTokens[0], headerTokens[1]);
        }

        if(httpMethod.isPost()) {
            this.requestPath = tokens[1];

            String body = IOUtils.readData(br, Integer.parseInt(getHeader("Content-Length")));
            parameter = HttpRequestUtils.parseQueryString(body);
        } else {
            int idx = tokens[1].indexOf("?");
            this.requestPath = tokens[1].substring(0, idx);

            String queryString = tokens[1].substring(idx + 1);
            parameter = HttpRequestUtils.parseQueryString(queryString);
        }
    }

    public String getMethod() {
        return this.httpMethod.name();
    }

    public String getPath() {
        return this.requestPath;
    }

    public String getHeader(String key) {
        return header.get(key);
    }

    public String getParameter(String key) {
        return parameter.get(key);
    }
}
