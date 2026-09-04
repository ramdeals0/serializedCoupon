package com.skillnet.serializedcoupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SerializedCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(SerializedCouponApplication.class, args);
    }
}
