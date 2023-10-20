package http;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//p196
public class HttpSession {
    private static Map<String, Object> values = new HashMap<>(); // 각 세션별 개별 생성

    private String id;
    public HttpSession(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAttribute(String name, Object value) {
        values.put(name, value);
    }

    public Object getAttribute(String name) {
        return values.get(name);
    }

    public void removeAttribute(String name) {
        values.remove(name);
    }

    public void invalidate() {
        HttpSessions.remove(id);
    }
}
