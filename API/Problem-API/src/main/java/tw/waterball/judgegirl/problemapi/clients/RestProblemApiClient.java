package tw.waterball.judgegirl.problemapi.clients;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import java.util.function.Supplier;

import static tw.waterball.judgegirl.api.utils.RestTemplateUtils.*;


/**
 * @author - wally55077@gmail.com
 */
public class RestProblemApiClient implements ProblemServiceDriver {

    public static final String API_PREFIX = "/api/problems";
    private final Supplier<String> tokenSupplier;
    private final RestTemplate restTemplate;

    public RestProblemApiClient(RestTemplate restTemplate, Supplier<String> tokenSupplier) {
        this.restTemplate = restTemplate;
        this.tokenSupplier = tokenSupplier;
    }

    @Override
    public ProblemView getProblem(int problemId) throws NotFoundException {
        String url = parsePath(API_PREFIX + "/{problemId}", problemId);
        return restTemplate.getForObject(url, ProblemView.class);
    }

    @Override
    public FileResource downloadProvidedCodes(int problemId, String languageEnvName, String providedCodesFileId) throws NotFoundException {
        String url = parsePath(API_PREFIX + "/{problemId}/{langEnvName}/providedCodes/{providedCodesFileId}",
                problemId, languageEnvName, providedCodesFileId);
        HttpEntity<?> entity = new HttpEntity<>(withBearerTokenHeader(tokenSupplier.get()));
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        return parseDownloadedFileResource(response);
    }

    @Override
    public FileResource downloadTestCaseIOs(int problemId, String testcaseIOsFileId) throws NotFoundException {
        String url = parsePath(API_PREFIX + "/{problemId}/testcaseIOs/{testcaseIOsFileId}",
                problemId, testcaseIOsFileId);
        HttpEntity<?> entity = new HttpEntity<>(withBearerTokenHeader(tokenSupplier.get()));
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        return parseDownloadedFileResource(response);
    }

}
