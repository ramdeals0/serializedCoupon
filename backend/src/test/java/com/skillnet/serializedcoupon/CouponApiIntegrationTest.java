package com.skillnet.serializedcoupon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillnet.serializedcoupon.domain.CouponBatch;
import com.skillnet.serializedcoupon.domain.CouponBatchStatus;
import com.skillnet.serializedcoupon.domain.RmsCouponDefinition;
import com.skillnet.serializedcoupon.domain.SerializedCoupon;
import com.skillnet.serializedcoupon.domain.SerializedCouponStatus;
import com.skillnet.serializedcoupon.repository.CouponBatchRepository;
import com.skillnet.serializedcoupon.repository.RmsCouponDefinitionRepository;
import com.skillnet.serializedcoupon.repository.SerializedCouponRepository;
import com.skillnet.serializedcoupon.service.CouponExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class CouponApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SerializedCouponRepository serializedCouponRepository;
    @Autowired
    private CouponBatchRepository couponBatchRepository;
    @Autowired
    private RmsCouponDefinitionRepository rmsCouponDefinitionRepository;

    @Test
    void createBatchPersistsCouponsAndSupportsIdempotencyPaginationExportAndErrors() throws Exception {
        String couponBody = """
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
        MvcResult couponCreated = mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(couponBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Fall 2026 BOGO"))
                .andExpect(jsonPath("$.couponProgramCode").value("1234"))
                .andReturn();
        String couponId = objectMapper.readTree(couponCreated.getResponse().getContentAsString()).get("id").asText();
        String body = """
                {
                  "couponId": "%s",
                  "quantity": 12
                }
                """.formatted(couponId);

        MvcResult created = mockMvc.perform(post("/api/v1/coupon-batches")
                        .header("Idempotency-Key", "test-key-create-12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generatedQuantity").value(12))
                .andExpect(jsonPath("$.couponProgramCode").value("1234"))
                .andExpect(jsonPath("$.rmsCouponId").value("RMS-COUPON-1001"))
                .andExpect(jsonPath("$.sampleCouponCodes").isArray())
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(created.getResponse().getContentAsString());
        String batchId = createdJson.get("id").asText();
        assertThat(createdJson.get("sampleCouponCodes").size()).isLessThanOrEqualTo(25);
        createdJson.get("sampleCouponCodes").forEach(code ->
                assertThat(code.asText()).matches("^FF[0-9]{4}[A-Z0-9]{8}$"));

        mockMvc.perform(post("/api/v1/coupon-batches")
                        .header("Idempotency-Key", "test-key-create-12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(batchId))
                .andExpect(jsonPath("$.generatedQuantity").value(12));

        mockMvc.perform(get("/api/v1/coupon-batches/{batchId}/coupons", batchId)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.content.length()").value(5));

        mockMvc.perform(get("/api/v1/serialized-coupons")
                        .param("couponProgramCode", "1234")
                        .param("rmsCouponId", "RMS-COUPON-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(12));

        MvcResult export = mockMvc.perform(get("/api/v1/coupon-batches/{batchId}/export", batchId))
                .andExpect(request().asyncStarted())
                .andReturn();
        export.getAsyncResult();
        String csv = mockMvc.perform(asyncDispatch(export))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(csv).startsWith(CouponExportService.CSV_HEADER);
        assertThat(csv).startsWith("couponCode,expiresAt\n");
        assertThat(csv).containsPattern("(?m)^FF[0-9]{4}[A-Z0-9]{8},2026-12-31T23:59:59Z$");
        assertThat(csv).doesNotContain("rmsCouponId");
        assertThat(csv.split("\n")).hasSizeGreaterThanOrEqualTo(13);
        assertThat(export.getResponse().getContentType()).contains("text/csv");
        assertThat(export.getResponse().getHeader("Content-Disposition")).contains("attachment");

        String firstCode = createdJson.get("sampleCouponCodes").get(0).asText();
        mockMvc.perform(post("/api/v1/serialized-coupons/{couponCode}/validate", firstCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponCode").value(firstCode))
                .andExpect(jsonPath("$.valid").value(true));

        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Fall 2026 BOGO",
                                  "usageLimit": 1,
                                  "couponProgramCode": "12",
                                  "posCode": "RMS-COUPON-1001",
                                  "atgCode": "FALL26-ATG",
                                  "couponSource": "BOTH",
                                  "startAt": "2026-09-01T00:00:00Z",
                                  "expiresAt": "2026-12-31T23:59:59Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Retired promotion",
                                  "usageLimit": 1,
                                  "couponProgramCode": "1234",
                                  "posCode": "RMS-COUPON-INACTIVE",
                                  "couponSource": "POS",
                                  "startAt": "2026-09-01T00:00:00Z",
                                  "expiresAt": "2026-12-31T23:59:59Z"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("RMS coupon inactive"));
    }

    @Test
    void customPosCodeCouponIsValidWhenLocalCatalogIsActive() throws Exception {
        MvcResult couponCreated = mockMvc.perform(post("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Custom POS offer",
                                  "usageLimit": 2,
                                  "couponProgramCode": "2323",
                                  "posCode": "C1234",
                                  "couponSource": "POS",
                                  "startAt": "2026-09-01T00:00:00Z",
                                  "expiresAt": "2026-12-31T23:59:59Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String couponId = objectMapper.readTree(couponCreated.getResponse().getContentAsString()).get("id").asText();

        MvcResult batch = mockMvc.perform(post("/api/v1/coupon-batches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"couponId\":\"%s\",\"quantity\":1}".formatted(couponId)))
                .andExpect(status().isCreated())
                .andReturn();
        String code = objectMapper.readTree(batch.getResponse().getContentAsString())
                .get("sampleCouponCodes").get(0).asText();

        mockMvc.perform(post("/api/v1/serialized-coupons/{couponCode}/validate", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.validationReason").value("VALID"));

        mockMvc.perform(get("/api/v1/serialized-coupons/{couponCode}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timesUsed").value(0))
                .andExpect(jsonPath("$.usageLimit").value(2));
    }

    @Test
    @Transactional
    void couponCodeUniqueConstraintIsEnforced() {
        RmsCouponDefinition rms = rmsCouponDefinitionRepository.findByRmsCouponId("RMS-COUPON-1001")
                .orElseThrow();
        CouponBatch batch = new CouponBatch();
        batch.setRmsCouponDefinition(rms);
        batch.setCouponProgramCode("5555");
        batch.setRequestedQuantity(2);
        batch.setGeneratedQuantity(1);
        batch.setStartAt(Instant.parse("2026-09-01T00:00:00Z"));
        batch.setExpiresAt(Instant.parse("2026-12-31T23:59:59Z"));
        batch.setStatus(CouponBatchStatus.COMPLETED);
        couponBatchRepository.saveAndFlush(batch);

        SerializedCoupon first = coupon(batch, rms, "FF5555ABCD2345");
        serializedCouponRepository.saveAndFlush(first);

        SerializedCoupon duplicate = coupon(batch, rms, "FF5555ABCD2345");
        assertThatThrownBy(() -> serializedCouponRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void dashboardAllowsConfiguredCorsOrigin() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard")
                        .header("Origin", "http://localhost:4200"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    private SerializedCoupon coupon(CouponBatch batch, RmsCouponDefinition rms, String code) {
        SerializedCoupon coupon = new SerializedCoupon();
        coupon.setCouponBatch(batch);
        coupon.setRmsCouponDefinition(rms);
        coupon.setCouponCode(code);
        coupon.setCouponProgramCode("5555");
        coupon.setStartAt(batch.getStartAt());
        coupon.setExpiresAt(batch.getExpiresAt());
        coupon.setStatus(SerializedCouponStatus.ACTIVE);
        coupon.setVersion(0L);
        return coupon;
    }
}
