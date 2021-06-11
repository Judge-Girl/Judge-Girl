package tw.waterball.judgegirl.springboot.advices.instrumentation;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@Component
public class InstrumentationClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @NotNull
    @Override
    public ClientHttpResponse intercept(@NotNull HttpRequest request, @NotNull byte[] body, @NotNull ClientHttpRequestExecution execution) throws IOException {
        String traceId = MDC.get("traceId");
        if (traceId != null) {
            request.getHeaders().add("traceId", traceId);
        }
        if (log.isTraceEnabled()) {
            log.trace("[Outgoing Http Request] {} {}", request.getMethod(), request.getURI());
        }
        var response = execution.execute(request, body);

        if (log.isTraceEnabled()) {
            log.trace("[Outgoing Http Response] {} ", response.getStatusCode());
        }
        return response;
    }
}
