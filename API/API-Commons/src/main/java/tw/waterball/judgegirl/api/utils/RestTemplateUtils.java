package tw.waterball.judgegirl.api.utils;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import tw.waterball.judgegirl.commons.models.files.FileResource;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - wally55077@gmail.com
 */
public class RestTemplateUtils {

    private RestTemplateUtils() {
    }

    public static String parsePath(String regexPath, Object... pathVariables) {
        return parsePath(regexPath, new LinkedList<>(asList(pathVariables)));
    }

    private static String parsePath(String regexPath, LinkedList<Object> pathVariables) {
        return stream(regexPath.split("/"))
                .map(path -> path.contains("{") && path.contains("}") ? pathVariables.removeFirst().toString() : path)
                .filter(path -> !path.isEmpty())
                .collect(joining("/"));
    }

    public static String parsePath(String regexPath, Map<String, String> queryParams) {
        StringBuilder resultPath = new StringBuilder(regexPath).append("?");
        queryParams.forEach((key, value) -> resultPath.append(key).append("=").append(value).append("&"));
        return resultPath.substring(0, resultPath.length() - 1);
    }

    public static HttpHeaders withBearerTokenHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    public static FileResource parseFileResourceFromResponse(ResponseEntity<byte[]> response) {
        HttpHeaders headers = response.getHeaders();
        ContentDisposition contentDisposition = headers.getContentDisposition();
        String fileName = contentDisposition.getFilename();
        byte[] body = response.getBody();
        if (body != null) {
            return new FileResource(fileName, headers.getContentLength(),
                    new ByteArrayInputStream(body));
        }
        throw notFound(fileName).message("response body is empty");
    }

}
