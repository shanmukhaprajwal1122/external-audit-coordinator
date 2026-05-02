package com.internship.tool.repository;

import com.internship.tool.entity.Role;
import com.internship.tool.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test") // Use a test profile if you have an application-test.yml (e.g. H2 config)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_Success() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setFullName("Test User");
        user.setPasswordHash("hashed123");
        user.setRole(Role.ADMIN);
        user.setIsActive(true);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test@email.com");

        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getFullName());
    }

    @Test
    void existsByEmail_ReturnsTrue() {
        User user = new User();
        user.setEmail("exists@email.com");
        user.setFullName("Test User 2");
        user.setPasswordHash("hashed123");
        user.setRole(Role.MANAGER);
        user.setIsActive(true);
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("exists@email.com");

        assertTrue(exists);
    }

    @Test
    void findByRole_Success() {
        User user1 = new User();
        user1.setEmail("admin1@email.com");
        user1.setFullName("Admin 1");
        user1.setPasswordHash("hashed123");
        user1.setRole(Role.ADMIN);
        user1.setIsActive(true);
        userRepository.save(user1);

        List<User> admins = userRepository.findByRole(Role.ADMIN);

        assertFalse(admins.isEmpty());
        assertTrue(admins.stream().allMatch(u -> u.getRole() == Role.ADMIN));
    }
}
