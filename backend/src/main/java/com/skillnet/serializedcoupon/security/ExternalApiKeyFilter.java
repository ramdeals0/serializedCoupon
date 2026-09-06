package com.skillnet.serializedcoupon.security;

import com.skillnet.serializedcoupon.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
public class ExternalApiKeyFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-Api-Key";

    private final AppProperties appProperties;

    public ExternalApiKeyFilter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null
                && request.getRequestURI().startsWith("/api/v1/external/")) {
            String configured = appProperties.getExternal().getApiKey();
            String provided = request.getHeader(API_KEY_HEADER);
            if (StringUtils.hasText(configured) && StringUtils.hasText(provided) && keysMatch(configured, provided)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                "external-pos",
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_EXTERNAL"))
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean keysMatch(String configured, String provided) {
        byte[] expected = configured.getBytes(StandardCharsets.UTF_8);
        byte[] actual = provided.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }
}
