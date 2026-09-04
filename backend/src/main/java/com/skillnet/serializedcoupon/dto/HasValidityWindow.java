package com.skillnet.serializedcoupon.dto;

import java.time.Instant;

public interface HasValidityWindow {

    Instant startAt();

    Instant expiresAt();
}
