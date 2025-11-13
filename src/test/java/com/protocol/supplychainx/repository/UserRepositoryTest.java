package com.protocol.supplychainx.repository;

import com.protocol.supplychainx.common.enums.RoleUtilisateur;
import com.protocol.supplychainx.user.entity.User;
import com.protocol.supplychainx.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email("test@repository.com")
                .password("encodedPassword")
                .role(RoleUtilisateur.ADMIN)
                .build();
    }

    @Test
    @DisplayName("Should save and find user by ID")
    void testSaveAndFindById() {
        // Act
        User savedUser = entityManager.persistAndFlush(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("Test", foundUser.get().getFirstName());
        assertEquals("test@repository.com", foundUser.get().getEmail());
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail() {
        // Arrange
        entityManager.persistAndFlush(user);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("test@repository.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("Test", foundUser.get().getFirstName());
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void testFindByEmail_NotFound() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@email.com");

        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmail() {
        // Arrange
        entityManager.persistAndFlush(user);

        // Act
        boolean exists = userRepository.existsByEmail("test@repository.com");
        boolean notExists = userRepository.existsByEmail("nonexistent@email.com");

        // Assert
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    @DisplayName("Should delete user by ID")
    void testDeleteUser() {
        // Arrange
        User savedUser = entityManager.persistAndFlush(user);
        Long userId = savedUser.getId();

        // Act
        userRepository.deleteById(userId);
        Optional<User> foundUser = userRepository.findById(userId);

        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("Should find all users")
    void testFindAll() {
        // Arrange
        User user2 = User.builder()
                .firstName("Second")
                .lastName("User")
                .email("second@repository.com")
                .password("password")
                .role(RoleUtilisateur.CHEF_PRODUCTION)
                .build();

        entityManager.persistAndFlush(user);
        entityManager.persistAndFlush(user2);

        // Act
        long count = userRepository.count();

        // Assert
        assertTrue(count >= 2);
    }
}

