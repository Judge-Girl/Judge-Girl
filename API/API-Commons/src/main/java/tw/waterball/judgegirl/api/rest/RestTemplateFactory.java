package tw.waterball.judgegirl.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static java.util.Arrays.asList;

/**
 * @author - wally55077@gmail.com
 */
public class RestTemplateFactory {

    private final ObjectMapper objectMapper;

    public RestTemplateFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RestTemplate create(String scheme, String host, int port, ClientHttpRequestInterceptor... interceptors) {
        if (host.endsWith("/")) {
            throw new IllegalArgumentException("The base url should not end with '/'");
        }
        RestTemplate restTemplate = new RestTemplate();
        URI baseUri = URI.create(String.format("%s://%s:%d", scheme, host, port));
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(baseUri);
        DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory(uriComponentsBuilder);
        restTemplate.setUriTemplateHandler(defaultUriBuilderFactory);
        restTemplate.setInterceptors(asList(interceptors));
        return restTemplate;
    }

}
