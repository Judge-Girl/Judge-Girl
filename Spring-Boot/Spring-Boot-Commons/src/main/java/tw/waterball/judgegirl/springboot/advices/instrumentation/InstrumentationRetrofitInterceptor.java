package tw.waterball.judgegirl.springboot.advices.instrumentation;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@Component
public class InstrumentationRetrofitInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        String traceId = MDC.get("traceId");
        Request request = chain.request().newBuilder()
                .addHeader("traceId", traceId)
                .build();

        if (log.isTraceEnabled()) {
            log.trace("[Outgoing Http Request] {} {}", request.method(), request.url());
        }
        var response = chain.proceed(request);

        if (log.isTraceEnabled()) {
            log.trace("[Outgoing Http Response] {}", response.code());
        }
        return response;
    }
}
