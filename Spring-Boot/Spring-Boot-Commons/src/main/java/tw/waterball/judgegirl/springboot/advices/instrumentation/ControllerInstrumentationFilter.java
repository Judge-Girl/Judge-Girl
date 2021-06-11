package tw.waterball.judgegirl.springboot.advices.instrumentation;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

import static java.util.Objects.requireNonNullElse;
import static tw.waterball.judgegirl.springboot.advices.instrumentation.TraceIds.generateTraceId;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class ControllerInstrumentationFilter extends GenericFilterBean {
    public static final Set<String> INCLUDED_HEADERS = Set.of("authorization", "user-agent", "host", "connection");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String traceId = requireNonNullElse(httpRequest.getHeader("traceId"), generateTraceId());
        String remoteAddr = httpRequest.getRemoteAddr();
        MDC.put("traceId", traceId);
        if (remoteAddr != null) {
            MDC.put("remoteAddr", remoteAddr);
        }
        log(httpRequest);

        filterChain.doFilter(servletRequest, servletResponse);

        MDC.remove("traceId");
        MDC.remove("remoteAddr");
    }

    private void log(HttpServletRequest httpRequest) {
        if (log.isTraceEnabled()) {
            var headerNamesEnumeration = httpRequest.getHeaderNames();
            StringBuilder headers = new StringBuilder();
            while (headerNamesEnumeration.hasMoreElements()) {
                String header = headerNamesEnumeration.nextElement();
                if (INCLUDED_HEADERS.contains(header)) {
                    String value = httpRequest.getHeader(header);
                    headers.append(header).append("=").append(value);
                    if (headerNamesEnumeration.hasMoreElements()) {
                        headers.append(" ");
                    }

                }
            }

            log.trace("[Incoming HTTP Request] {} {} Headers: {}, Body: content-length={} content-type={}", httpRequest.getMethod(),
                    httpRequest.getRequestURL(), headers, httpRequest.getContentLengthLong(), httpRequest.getContentType());
        }
    }
}