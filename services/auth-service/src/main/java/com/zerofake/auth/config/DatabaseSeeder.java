package com.zerofake.auth.config;

import com.zerofake.auth.constant.RoleType;
import com.zerofake.auth.constant.UserStatus;
import com.zerofake.auth.entity.User;
import com.zerofake.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedUser("Admin", "User", "admin@zerofake.com", "Password123!", RoleType.ROLE_ADMIN);
        seedUser("Manufacturer", "User", "manufacturer@zerofake.com", "Password123!", RoleType.ROLE_MANUFACTURER);
        seedUser("Warehouse", "User", "warehouse@zerofake.com", "Password123!", RoleType.ROLE_WAREHOUSE);
        seedUser("Distributor", "User", "distributor@zerofake.com", "Password123!", RoleType.ROLE_DISTRIBUTOR);
        seedUser("Retailer", "User", "retailer@zerofake.com", "Password123!", RoleType.ROLE_RETAILER);
        seedUser("Customer", "User", "customer@zerofake.com", "Password123!", RoleType.ROLE_CUSTOMER);
    }

    private void seedUser(String firstName, String lastName, String email, String password, RoleType role) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
            System.out.println("Seeded user: " + email + " with role: " + role);
        }
    }
}
