package db;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import model.User;

public class DataBase {
    private static Map<String, User> users;

    static {
        users = Maps.newHashMap();
        users.put("test1", new User("test1", "1234", "테스터1", ""));
        users.put("test2", new User("test2", "1234", "테스터2", ""));
        users.put("test3", new User("test3", "1234", "테스터3", ""));
    }

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static Collection<User> findAll() {
        return users.values();
    }
}
