package com.zerofake.auth.repository;
import com.zerofake.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface UserRepository extends JpaRepository<User, UUID>{
    Optional<User> findByEmail(String email);
    boolean existedByEmail(String email);
}
