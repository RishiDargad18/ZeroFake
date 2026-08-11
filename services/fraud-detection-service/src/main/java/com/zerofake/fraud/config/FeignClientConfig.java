package com.zerofake.fraud.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Propagates the caller's bearer token to downstream services.
 *
 * <p>This service holds no credentials of its own. Every downstream call is made
 * with the identity of the user who initiated the request, so the product and
 * blockchain services apply exactly the authorization they would for a direct
 * call and this service cannot be used to escalate privilege.
 */
public class FeignClientConfig {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Bean
    public RequestInterceptor bearerTokenPropagationInterceptor() {

        return template -> {

            if (!(RequestContextHolder.getRequestAttributes()
                    instanceof ServletRequestAttributes attributes)) {
                return;
            }

            String authorization =
                    attributes.getRequest().getHeader(AUTHORIZATION_HEADER);

            if (authorization != null && !authorization.isBlank()) {
                template.header(AUTHORIZATION_HEADER, authorization);
            }
        };
    }
}
