package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class HttpParams {
    private static final Logger log = LoggerFactory.getLogger(HttpParams.class);
    private Map<String, String> params = new HashMap<>();

    public HttpParams() {}

    public void addQueryString(String queryString) {
        putParam(queryString);
    }
    public String getParameter(String key) {
        return params.get(key);
    }

    public void putParam(String data) {
        log.debug("data : {}", data);

        if(data == null || data.isEmpty()) return;

        params.putAll(HttpRequestUtils.parseQueryString(data));
        log.debug("params: {}", params);
    }

    public void addBody(String body) {
        putParam(body);
    }
}
