package com.zerofake.auth.config;

import com.zerofake.auth.constant.RoleType;
import com.zerofake.auth.constant.UserStatus;
import com.zerofake.auth.entity.User;
import com.zerofake.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds one demonstration account per role so that the platform can be exercised
 * immediately after a fresh deployment.
 *
 * <p>Disable with {@code zerofake.seed.enabled=false} and always override
 * {@code zerofake.seed.default-password} outside local development.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "zerofake.seed",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${zerofake.seed.default-password}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        seedUser("Admin", "User", "admin@zerofake.com", RoleType.ROLE_ADMIN);
        seedUser("Manufacturer", "User", "manufacturer@zerofake.com", RoleType.ROLE_MANUFACTURER);
        seedUser("Warehouse", "User", "warehouse@zerofake.com", RoleType.ROLE_WAREHOUSE);
        seedUser("Distributor", "User", "distributor@zerofake.com", RoleType.ROLE_DISTRIBUTOR);
        seedUser("Retailer", "User", "retailer@zerofake.com", RoleType.ROLE_RETAILER);
        seedUser("Customer", "User", "customer@zerofake.com", RoleType.ROLE_CUSTOMER);
    }

    private void seedUser(
            String firstName,
            String lastName,
            String email,
            RoleType role
    ) {

        if (userRepository.existsByEmail(email)) {
            return;
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(defaultPassword));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        log.info("Seeded demonstration user {} with role {}", email, role);
    }
}
