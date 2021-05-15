package tw.waterball.judgegirl.api.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static tw.waterball.judgegirl.commons.utils.HttpHeaderUtils.bearerWithToken;
import static tw.waterball.judgegirl.commons.utils.HttpHeaderUtils.parseFileNameFromContentDisposition;

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
        return Arrays.stream(regexPath.split("/"))
                .map(path -> path.contains("{") && path.contains("}") ? pathVariables.removeFirst().toString() : path)
                .filter(path -> !path.isEmpty())
                .collect(Collectors.joining("/"));
    }

    public static HttpHeaders withBearerTokenHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", bearerWithToken(token));
        return headers;
    }

    public static FileResource parseDownloadedFileResource(ResponseEntity<byte[]> response) {
        HttpHeaders headers = response.getHeaders();
        String fileName = parseFileNameFromContentDisposition(requireNonNull(headers.getFirst("Content-Disposition")));
        try {
            return new FileResource(fileName, parseLong(requireNonNull(headers.getFirst("Content-Length"))),
                    new ByteArrayInputStream(requireNonNull(response.getBody())));
        } catch (Exception e) {
            throw NotFoundException.notFound(fileName).message(e);
        }
    }

}
