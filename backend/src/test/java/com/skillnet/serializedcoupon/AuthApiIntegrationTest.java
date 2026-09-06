package com.skillnet.serializedcoupon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthApiIntegrationTest {

    private static final String COUPON_BODY = """
            {
              "title": "Fall 2026 BOGO",
              "description": "Buy one get one serialized coupon program",
              "usageLimit": 1,
              "couponProgramCode": "1234",
              "posCode": "RMS-COUPON-1001",
              "atgCode": "FALL26-ATG",
              "couponSource": "BOTH",
              "startAt": "2026-09-01T00:00:00Z",
              "expiresAt": "2026-12-31T23:59:59Z"
            }
            """;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginRejectsBadCredentialsAndIssuesJwtForKnownUsers() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isUnauthorized());

        String token = login("admin", "Admin123!");
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void managerCanCreateButCannotOpenDashboard() throws Exception {
        String token = login("manager", "Manager123!");
        mockMvc.perform(get("/api/v1/dashboard").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        String couponId = createCoupon(token);
        MvcResult created = mockMvc.perform(post("/api/v1/coupon-batches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchBody(couponId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdBy").value("manager"))
                .andReturn();
        assertThat(objectMapper.readTree(created.getResponse().getContentAsString()).get("generatedQuantity").asInt())
                .isEqualTo(2);
    }

    @Test
    void customerServiceCanViewCouponStatusButCannotCreateOrDeactivate() throws Exception {
        String adminToken = login("admin", "Admin123!");
        String couponId = createCoupon(adminToken);
        MvcResult created = mockMvc.perform(post("/api/v1/coupon-batches")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchBody(couponId)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode createdJson = objectMapper.readTree(created.getResponse().getContentAsString());
        String couponCode = createdJson.get("sampleCouponCodes").get(0).asText();

        String csrToken = login("csr", "Csr123!");
        mockMvc.perform(get("/api/v1/serialized-coupons/" + couponCode)
                        .header("Authorization", "Bearer " + csrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponCode").value(couponCode));

        mockMvc.perform(post("/api/v1/serialized-coupons/" + couponCode + "/validate")
                        .header("Authorization", "Bearer " + csrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponCode").value(couponCode));

        mockMvc.perform(post("/api/v1/coupons")
                        .header("Authorization", "Bearer " + csrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COUPON_BODY))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/coupon-batches")
                        .header("Authorization", "Bearer " + csrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchBody(couponId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/serialized-coupons/" + couponCode + "/deactivate")
                        .header("Authorization", "Bearer " + csrToken))
                .andExpect(status().isForbidden());
    }

    private String createCoupon(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/coupons")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(COUPON_BODY))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String batchBody(String couponId) {
        return """
                {
                  "couponId": "%s",
                  "quantity": 2
                }
                """.formatted(couponId);
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
