package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.domain.RmsCouponDefinition;
import com.skillnet.serializedcoupon.dto.PageResponse;
import com.skillnet.serializedcoupon.dto.RmsCouponDefinitionResponse;
import com.skillnet.serializedcoupon.exception.ResourceNotFoundException;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponClient;
import com.skillnet.serializedcoupon.integration.rms.RmsCouponDetails;
import com.skillnet.serializedcoupon.mapper.CouponMapper;
import com.skillnet.serializedcoupon.repository.RmsCouponDefinitionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class RmsCouponDefinitionService {

    private final RmsCouponDefinitionRepository repository;
    private final RmsCouponClient rmsCouponClient;
    private final CouponMapper couponMapper;

    public RmsCouponDefinitionService(
            RmsCouponDefinitionRepository repository,
            RmsCouponClient rmsCouponClient,
            CouponMapper couponMapper
    ) {
        this.repository = repository;
        this.rmsCouponClient = rmsCouponClient;
        this.couponMapper = couponMapper;
    }

    @Transactional
    public RmsCouponDefinition synchronize(RmsCouponDetails details) {
        RmsCouponDefinition entity = repository.findByRmsCouponId(details.rmsCouponId())
                .orElseGet(RmsCouponDefinition::new);
        entity.setRmsCouponId(details.rmsCouponId());
        entity.setRmsCouponCode(details.rmsCouponCode());
        entity.setName(details.name());
        entity.setDescription(details.description());
        entity.setActive(details.active());
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public PageResponse<RmsCouponDefinitionResponse> search(String query, Boolean active, Pageable pageable) {
        Specification<RmsCouponDefinition> spec = (root, q, cb) -> cb.conjunction();
        if (active != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("active"), active));
        }
        if (StringUtils.hasText(query)) {
            String like = "%" + query.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("rmsCouponId")), like),
                    cb.like(cb.lower(root.get("rmsCouponCode")), like),
                    cb.like(cb.lower(root.get("name")), like)
            ));
        }
        Page<RmsCouponDefinitionResponse> page = repository.findAll(spec, pageable).map(couponMapper::toResponse);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public RmsCouponDefinitionResponse getByRmsCouponId(String rmsCouponId) {
        return repository.findByRmsCouponId(rmsCouponId)
                .map(couponMapper::toResponse)
                .orElseGet(() -> rmsCouponClient.getCouponDefinition(rmsCouponId)
                        .map(details -> new RmsCouponDefinitionResponse(
                                null,
                                details.rmsCouponId(),
                                details.rmsCouponCode(),
                                details.name(),
                                details.description(),
                                details.active(),
                                null,
                                null
                        ))
                        .orElseThrow(() -> new ResourceNotFoundException("RMS coupon not found: " + rmsCouponId)));
    }
}
