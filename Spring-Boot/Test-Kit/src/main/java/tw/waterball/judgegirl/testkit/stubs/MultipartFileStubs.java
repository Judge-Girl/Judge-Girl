package tw.waterball.judgegirl.testkit.stubs;

import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class MultipartFileStubs {
    public static MockMultipartFile[] codes(String name, int fileCount) {
        MockMultipartFile[] files = new MockMultipartFile[fileCount];
        for (int i = 0; i < fileCount; i++) {
            files[i] = new MockMultipartFile(name, "func1.c", "text/plain",
                    "int plus(int a, int b) {return a + b;}".getBytes(StandardCharsets.UTF_8));
        }
        return files;
    }
}
