package com.skillnet.serializedcoupon;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExternalCouponApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void apiKeyCanValidateThenRedeemAndRejectReuse() throws Exception {
        String token = login();
        String couponId = createCoupon(token);
        MvcResult batch = mockMvc.perform(post("/api/v1/coupon-batches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"couponId\":\"" + couponId + "\",\"quantity\":1}"))
                .andExpect(status().isCreated())
                .andReturn();
        String code = objectMapper.readTree(batch.getResponse().getContentAsString())
                .get("sampleCouponCodes").get(0).asText();

        mockMvc.perform(post("/api/v1/external/coupons/validate")
                        .header("X-Api-Key", "pos-demo-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"couponCode\":\"" + code + "\",\"channel\":\"POS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(true))
                .andExpect(jsonPath("$.markedUsed").value(false))
                .andExpect(jsonPath("$.validationReason").value("VALID"));

        mockMvc.perform(post("/api/v1/external/coupons/redeem")
                        .header("X-Api-Key", "pos-demo-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"couponCode\":\"" + code + "\",\"channel\":\"POS\",\"locationId\":\"STORE-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(true))
                .andExpect(jsonPath("$.markedUsed").value(true))
                .andExpect(jsonPath("$.status").value("REDEEMED"))
                .andExpect(jsonPath("$.timesUsed").value(1));

        mockMvc.perform(post("/api/v1/external/coupons/redeem")
                        .header("X-Api-Key", "pos-demo-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"couponCode\":\"" + code + "\",\"channel\":\"POS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(false))
                .andExpect(jsonPath("$.markedUsed").value(false))
                .andExpect(jsonPath("$.validationReason").value("REDEEMED"));
    }

    @Test
    void rejectsMissingApiKey() throws Exception {
        mockMvc.perform(post("/api/v1/external/coupons/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"couponCode\":\"FF1234ABCD2345\",\"channel\":\"POS\"}"))
                .andExpect(status().isUnauthorized());
    }

    private String createCoupon(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/coupons")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "POS demo",
                                  "usageLimit": 1,
                                  "couponProgramCode": "4321",
                                  "posCode": "RMS-COUPON-1001",
                                  "atgCode": "FALL26-ATG",
                                  "couponSource": "BOTH",
                                  "startAt": "2026-09-01T00:00:00Z",
                                  "expiresAt": "2026-12-31T23:59:59Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"Admin123!\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
