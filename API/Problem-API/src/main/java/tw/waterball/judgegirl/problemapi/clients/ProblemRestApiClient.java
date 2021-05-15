package tw.waterball.judgegirl.problemapi.clients;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import tw.waterball.judgegirl.api.rest.RestTemplateFactory;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static tw.waterball.judgegirl.commons.utils.HttpHeaderUtils.bearerWithToken;
import static tw.waterball.judgegirl.commons.utils.HttpHeaderUtils.parseFileNameFromContentDisposition;


/**
 * @author - wally55077@gmail.com
 */
public class ProblemRestApiClient implements ProblemServiceDriver {

    public static final String API_PROBLEMS = "/api/problems";
    private final Supplier<String> tokenSupplier;
    private final RestTemplate restTemplate;

    public ProblemRestApiClient(RestTemplateFactory restTemplateFactory,
                                String scheme,
                                String host, int port,
                                Supplier<String> tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
        this.restTemplate = restTemplateFactory.create(scheme, host, port);
    }

    @Override
    public ProblemView getProblem(int problemId) throws NotFoundException {
        String url = parsePath(API_PROBLEMS + "/{problemId}", problemId);
        return restTemplate.getForObject(url, ProblemView.class);
    }

    @Override
    public FileResource downloadProvidedCodes(int problemId, String languageEnvName, String providedCodesFileId) throws NotFoundException {
        String url = parsePath(API_PROBLEMS + "/{problemId}/{langEnvName}/providedCodes/{providedCodesFileId}",
                problemId, languageEnvName, providedCodesFileId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", bearerWithToken(tokenSupplier.get()));
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        return parseDownloadedFileResource(response);
    }

    @Override
    public FileResource downloadTestCaseIOs(int problemId, String testcaseIOsFileId) throws NotFoundException {
        String url = parsePath(API_PROBLEMS + "/{problemId}/testcaseIOs/{testcaseIOsFileId}",
                problemId, testcaseIOsFileId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", bearerWithToken(tokenSupplier.get()));
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        return parseDownloadedFileResource(response);
    }

    protected FileResource parseDownloadedFileResource(ResponseEntity<byte[]> response) {
        HttpHeaders headers = response.getHeaders();
        String fileName = parseFileNameFromContentDisposition(requireNonNull(headers.getFirst("Content-Disposition")));
        try {
            return new FileResource(fileName, parseLong(requireNonNull(headers.getFirst("Content-Length"))),
                    new ByteArrayInputStream(requireNonNull(response.getBody())));
        } catch (Exception e) {
            throw NotFoundException.notFound(fileName).message(e);
        }
    }

    private String parsePath(String regexPath, Object... pathVariables) {
        return parsePath(regexPath, new LinkedList<>(asList(pathVariables)));
    }

    private String parsePath(String regexPath, LinkedList<Object> pathVariables) {
        return Arrays.stream(regexPath.split("/"))
                .map(path -> path.contains("{") && path.contains("}") ? pathVariables.removeFirst().toString() : path)
                .filter(path -> !path.isEmpty())
                .collect(Collectors.joining("/"));
    }

}
