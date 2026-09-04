package com.skillnet.serializedcoupon.integration.rms;

import com.skillnet.serializedcoupon.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * Placeholder HTTP adapter for a future RMS coupon lookup API.
 *
 * Expected remote contract (replace when the real RMS API is known):
 *   GET {app.rms.base-url}/coupon-definitions/{rmsCouponId}
 *
 * Authentication uses RMS_API_KEY as a bearer token when provided.
 * Credentials must come from environment variables, never source control.
 */
@Component
@ConditionalOnProperty(name = "app.rms.client", havingValue = "rest")
public class RestRmsCouponClient implements RmsCouponClient {

    private static final Logger log = LoggerFactory.getLogger(RestRmsCouponClient.class);

    private final RestClient restClient;

    public RestRmsCouponClient(AppProperties appProperties) {
        String baseUrl = appProperties.getRms().getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("app.rms.base-url is required when app.rms.client=rest");
        }
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
        String apiKey = appProperties.getRms().getApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        }
        this.restClient = builder.build();
    }

    @Override
    public Optional<RmsCouponDetails> getCouponDefinition(String rmsCouponId) {
        try {
            RmsCouponDetails details = restClient.get()
                    .uri("/coupon-definitions/{rmsCouponId}", rmsCouponId)
                    .retrieve()
                    .body(RmsCouponDetails.class);
            return Optional.ofNullable(details);
        } catch (RestClientException ex) {
            log.warn("RMS lookup failed for couponId={}", rmsCouponId);
            return Optional.empty();
        }
    }

    @Override
    public boolean couponDefinitionExistsAndIsActive(String rmsCouponId) {
        return getCouponDefinition(rmsCouponId).map(RmsCouponDetails::active).orElse(false);
    }
}
