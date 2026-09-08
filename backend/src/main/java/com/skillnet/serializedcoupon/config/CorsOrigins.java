package com.skillnet.serializedcoupon.config;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class CorsOrigins {

    private CorsOrigins() {
    }

    public static String[] resolve(Collection<String> configured, String... extraHosts) {
        Set<String> origins = new LinkedHashSet<>();
        if (configured != null) {
            for (String raw : configured) {
                addRaw(origins, raw);
            }
        }
        if (extraHosts != null) {
            for (String host : extraHosts) {
                addHost(origins, host);
            }
        }
        return origins.toArray(String[]::new);
    }

    private static void addRaw(Set<String> origins, String raw) {
        if (raw == null || raw.isBlank()) {
            return;
        }
        for (String part : raw.split("[,\\s]+")) {
            addHost(origins, part);
        }
    }

    private static void addHost(Set<String> origins, String host) {
        if (host == null || host.isBlank()) {
            return;
        }
        String value = host.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        if (value.isEmpty()) {
            return;
        }
        if (value.startsWith("http://") || value.startsWith("https://")) {
            origins.add(value);
            return;
        }
        origins.add("https://" + value);
    }
}
