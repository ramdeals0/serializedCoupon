package com.skillnet.serializedcoupon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Cors cors = new Cors();
    private final Rms rms = new Rms();
    private final Auth auth = new Auth();
    private final External external = new External();

    public Cors getCors() {
        return cors;
    }

    public Rms getRms() {
        return rms;
    }

    public Auth getAuth() {
        return auth;
    }

    public External getExternal() {
        return external;
    }

    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:4200", "http://127.0.0.1:4200"));
        private String publicDomain = "";
        private String staticUrl = "";

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public String getPublicDomain() {
            return publicDomain;
        }

        public void setPublicDomain(String publicDomain) {
            this.publicDomain = publicDomain;
        }

        public String getStaticUrl() {
            return staticUrl;
        }

        public void setStaticUrl(String staticUrl) {
            this.staticUrl = staticUrl;
        }
    }

    public static class Rms {
        /**
         * mock | rest
         */
        private String client = "mock";
        private String baseUrl = "";
        private String apiKey = "";

        public String getClient() {
            return client;
        }

        public void setClient(String client) {
            this.client = client;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    public static class Auth {
        private String jwtSecret = "local-dev-jwt-secret-change-me-32bytes-min";
        private java.time.Duration jwtExpiration = java.time.Duration.ofHours(8);
        private String adminUsername = "admin";
        private String adminPassword = "Admin123!";
        private String adminDisplayName = "Administrator";
        private String managerUsername = "manager";
        private String managerPassword = "Manager123!";
        private String managerDisplayName = "Manager";
        private String customerServiceUsername = "csr";
        private String customerServicePassword = "Csr123!";
        private String customerServiceDisplayName = "Customer Service";

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public java.time.Duration getJwtExpiration() {
            return jwtExpiration;
        }

        public void setJwtExpiration(java.time.Duration jwtExpiration) {
            this.jwtExpiration = jwtExpiration;
        }

        public String getAdminUsername() {
            return adminUsername;
        }

        public void setAdminUsername(String adminUsername) {
            this.adminUsername = adminUsername;
        }

        public String getAdminPassword() {
            return adminPassword;
        }

        public void setAdminPassword(String adminPassword) {
            this.adminPassword = adminPassword;
        }

        public String getAdminDisplayName() {
            return adminDisplayName;
        }

        public void setAdminDisplayName(String adminDisplayName) {
            this.adminDisplayName = adminDisplayName;
        }

        public String getManagerUsername() {
            return managerUsername;
        }

        public void setManagerUsername(String managerUsername) {
            this.managerUsername = managerUsername;
        }

        public String getManagerPassword() {
            return managerPassword;
        }

        public void setManagerPassword(String managerPassword) {
            this.managerPassword = managerPassword;
        }

        public String getManagerDisplayName() {
            return managerDisplayName;
        }

        public void setManagerDisplayName(String managerDisplayName) {
            this.managerDisplayName = managerDisplayName;
        }

        public String getCustomerServiceUsername() {
            return customerServiceUsername;
        }

        public void setCustomerServiceUsername(String customerServiceUsername) {
            this.customerServiceUsername = customerServiceUsername;
        }

        public String getCustomerServicePassword() {
            return customerServicePassword;
        }

        public void setCustomerServicePassword(String customerServicePassword) {
            this.customerServicePassword = customerServicePassword;
        }

        public String getCustomerServiceDisplayName() {
            return customerServiceDisplayName;
        }

        public void setCustomerServiceDisplayName(String customerServiceDisplayName) {
            this.customerServiceDisplayName = customerServiceDisplayName;
        }
    }

    public static class External {
        private String apiKey = "pos-demo-key";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
