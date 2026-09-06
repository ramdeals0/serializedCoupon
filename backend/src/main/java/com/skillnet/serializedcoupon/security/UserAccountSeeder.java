package com.skillnet.serializedcoupon.security;

import com.skillnet.serializedcoupon.config.AppProperties;
import com.skillnet.serializedcoupon.domain.AppUser;
import com.skillnet.serializedcoupon.domain.UserRole;
import com.skillnet.serializedcoupon.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserAccountSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserAccountSeeder.class);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public UserAccountSeeder(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            AppProperties appProperties
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppProperties.Auth auth = appProperties.getAuth();
        seed(auth.getAdminUsername(), auth.getAdminPassword(), auth.getAdminDisplayName(), UserRole.ADMIN);
        seed(auth.getManagerUsername(), auth.getManagerPassword(), auth.getManagerDisplayName(), UserRole.MANAGER);
        seed(
                auth.getCustomerServiceUsername(),
                auth.getCustomerServicePassword(),
                auth.getCustomerServiceDisplayName(),
                UserRole.CUSTOMER_SERVICE
        );
    }

    private void seed(String username, String password, String displayName, UserRole role) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return;
        }
        appUserRepository.findByUsernameIgnoreCase(username.trim()).ifPresentOrElse(
                existing -> { },
                () -> {
                    AppUser user = new AppUser();
                    user.setUsername(username.trim());
                    user.setPasswordHash(passwordEncoder.encode(password));
                    user.setDisplayName(displayName == null || displayName.isBlank() ? role.name() : displayName);
                    user.setRole(role);
                    user.setEnabled(true);
                    appUserRepository.save(user);
                    log.info("Seeded {} account '{}'", role, username.trim());
                }
        );
    }
}
