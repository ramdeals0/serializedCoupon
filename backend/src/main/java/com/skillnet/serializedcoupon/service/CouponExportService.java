package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.domain.SerializedCoupon;
import com.skillnet.serializedcoupon.exception.ResourceNotFoundException;
import com.skillnet.serializedcoupon.repository.CouponBatchRepository;
import com.skillnet.serializedcoupon.repository.SerializedCouponRepository;
import com.skillnet.serializedcoupon.repository.SerializedCouponSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.time.Instant;
import java.util.UUID;

@Service
public class CouponExportService {

    public static final String CSV_HEADER = "couponCode,expiresAt";

    private final CouponBatchRepository couponBatchRepository;
    private final SerializedCouponRepository serializedCouponRepository;

    public CouponExportService(
            CouponBatchRepository couponBatchRepository,
            SerializedCouponRepository serializedCouponRepository
    ) {
        this.couponBatchRepository = couponBatchRepository;
        this.serializedCouponRepository = serializedCouponRepository;
    }

    @Transactional(readOnly = true)
    public void writeBatchCsv(UUID batchId, Writer writer) {
        if (!couponBatchRepository.existsById(batchId)) {
            throw new ResourceNotFoundException("Coupon batch not found: " + batchId);
        }
        try {
            writer.write(CSV_HEADER);
            writer.write('\n');
            Specification<SerializedCoupon> spec = SerializedCouponSpecifications.batchIdEquals(batchId);
            int pageNumber = 0;
            Page<SerializedCoupon> page;
            do {
                page = serializedCouponRepository.findAll(
                        spec,
                        PageRequest.of(pageNumber, 500, Sort.by(Sort.Direction.ASC, "createdAt"))
                );
                for (SerializedCoupon coupon : page.getContent()) {
                    writer.write(toCsvRow(coupon));
                    writer.write('\n');
                }
                pageNumber++;
            } while (page.hasNext());
            writer.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to export coupons for batch " + batchId, ex);
        }
    }

    private String toCsvRow(SerializedCoupon coupon) {
        return String.join(",",
                csv(coupon.getCouponCode()),
                csv(instant(coupon.getExpiresAt()))
        );
    }

    private String instant(Instant value) {
        return value == null ? "" : value.toString();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
