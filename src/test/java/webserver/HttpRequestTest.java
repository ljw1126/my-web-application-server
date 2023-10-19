package webserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class HttpRequestTest {
    private String testDirectory = "./src/test/resources/";

    @DisplayName("")
    @Test
    void fileResource() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("HTTP_GET.txt").getFile());
        assertThat(file).isNotNull();

        file = new File(testDirectory + "HTTP_GET.txt");
        assertThat(file).isNotNull();
    }

    @DisplayName("")
    @Test
    void requestGET() throws IOException {
        InputStream in = new FileInputStream(new File(testDirectory + "HTTP_GET.txt"));
        HttpRequest httpRequest = new HttpRequest(in);

        assertThat(httpRequest.getMethod()).isEqualTo("GET");
        assertThat(httpRequest.getPath()).isEqualTo("/user/create");
        assertThat(httpRequest.getHeader("Connection")).isEqualTo("keep-alive");
        assertThat(httpRequest.getParameter("userId")).isEqualTo("javajigi");
    }

    @DisplayName("")
    @Test
    void requestPOST() throws IOException {
        InputStream in = new FileInputStream(new File(testDirectory + "HTTP_POST.txt"));
        HttpRequest httpRequest = new HttpRequest(in);

        assertThat(httpRequest.getMethod()).isEqualTo("POST");
        assertThat(httpRequest.getPath()).isEqualTo("/user/create");
        assertThat(httpRequest.getHeader("Connection")).isEqualTo("keep-alive");
        assertThat(httpRequest.getParameter("userId")).isEqualTo("javajigi");
    }
}