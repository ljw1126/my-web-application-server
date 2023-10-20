package webserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class UUIDTest {
    @DisplayName("")
    @Test
    void uuid() {
        System.out.println(UUID.randomUUID());
    }
}
