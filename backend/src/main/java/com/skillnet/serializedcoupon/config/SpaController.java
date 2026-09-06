package com.skillnet.serializedcoupon.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the Angular app for browser routes so deep links work behind a single Railway URL.
 * API, actuator, and OpenAPI paths are left to their own controllers.
 */
@Controller
public class SpaController {

    @GetMapping({
            "/",
            "/login",
            "/pos-demo",
            "/coupons",
            "/coupons/{*path}",
            "/coupon-batches",
            "/coupon-batches/{*path}",
            "/serialized-coupons",
            "/serialized-coupons/{*path}"
    })
    public String forwardAngularRoutes() {
        return "forward:/index.html";
    }
}
