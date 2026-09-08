package com.skillnet.serializedcoupon.security;

import com.skillnet.serializedcoupon.config.AppProperties;
import com.skillnet.serializedcoupon.config.CorsOrigins;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ExternalApiKeyFilter externalApiKeyFilter;
    private final RestAuthHandlers restAuthHandlers;
    private final AppProperties appProperties;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ExternalApiKeyFilter externalApiKeyFilter,
            RestAuthHandlers restAuthHandlers,
            AppProperties appProperties
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.externalApiKeyFilter = externalApiKeyFilter;
        this.restAuthHandlers = restAuthHandlers;
        this.appProperties = appProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(restAuthHandlers)
                        .accessDeniedHandler(restAuthHandlers))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/h2-console", "/h2-console/**").permitAll()
                        .requestMatchers("/api/v1/dashboard").hasRole("ADMIN")
                        .requestMatchers("/api/v1/meta/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/coupons", "/api/v1/coupons/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/coupon-batches").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/coupon-batches/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/serialized-coupons/*/deactivate").hasRole("ADMIN")
                        .requestMatchers("/api/v1/serialized-coupons/**").hasAnyRole("ADMIN", "MANAGER", "CUSTOMER_SERVICE")
                        .requestMatchers("/api/v1/external/**").hasAnyRole("ADMIN", "MANAGER", "CUSTOMER_SERVICE", "EXTERNAL")
                        .requestMatchers("/api/v1/rms-coupons/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/auth/me").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(externalApiKeyFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        AppProperties.Cors cors = appProperties.getCors();
        String[] origins = CorsOrigins.resolve(
                cors.getAllowedOrigins(),
                cors.getPublicDomain(),
                cors.getStaticUrl()
        );
        if (origins.length == 0) {
            origins = new String[] {"http://localhost:4200", "http://127.0.0.1:4200"};
        }
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(origins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Content-Disposition", "Location", "Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
