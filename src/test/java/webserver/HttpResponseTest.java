package webserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class HttpResponseTest {
    private String testDirectory = "./src/test/resources/";

    @DisplayName("")
    @Test
    void responseForward() throws IOException {
        //HTTP_Forward.txt 결과는 응답 body에 index.html이 포함되어 있어야 한다.
        HttpResponse response = new HttpResponse(createOutputStream("Http_Forward.txt")); // *해당 위치에 파일을 생성함
        response.forward("/index.html");
    }

    @DisplayName("")
    @Test
    void responseRedirect() throws IOException {
        //HTTP_Redirect.txt 결과는 Location: index.html 정보가 포함되어 있어야 한다.
        HttpResponse response = new HttpResponse(createOutputStream("HTTP_Redirect.txt"));
        response.sendRedirect("/index.html");
    }

    @DisplayName("")
    @Test
    void responseCookies() throws IOException {
        //HTTP_Cookie.txt 결과는 응답 body에 index.html이 포함되어 있어야 한다.
        HttpResponse response = new HttpResponse(createOutputStream("HTTP_Cookies.txt"));
        response.addHeader("Set-Cookie", "logined= true");
        response.sendRedirect("/index.html");
    }

    private OutputStream createOutputStream(String fileName) throws FileNotFoundException {
        return new FileOutputStream(new File(testDirectory + fileName));
    }
}