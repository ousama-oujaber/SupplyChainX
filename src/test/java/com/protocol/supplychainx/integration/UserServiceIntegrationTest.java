package com.protocol.supplychainx.integration;

import com.protocol.supplychainx.common.enums.RoleUtilisateur;
import com.protocol.supplychainx.common.exceptions.user.EmailAlreadyExistsException;
import com.protocol.supplychainx.common.exceptions.user.UserNotFoundException;
import com.protocol.supplychainx.user.dto.UserDTO;
import com.protocol.supplychainx.user.service.IUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private IUserService userService;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("Integration: Should create and retrieve user successfully")
    void testCreateAndRetrieveUser() {
        // Arrange
        UserDTO userDTO = UserDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@test.com")
                .password("Password123")
                .role(RoleUtilisateur.CHEF_PRODUCTION)
                .build();

        // Act
        UserDTO createdUser = userService.createUser(userDTO);
        UserDTO retrievedUser = userService.getUser(createdUser.getId());

        // Assert
        assertNotNull(createdUser.getId());
        assertEquals("Jane", retrievedUser.getFirstName());
        assertEquals("jane.smith@test.com", retrievedUser.getEmail());
    }

    @Test
    @DisplayName("Integration: Should throw EmailAlreadyExistsException for duplicate email")
    void testCreateUserWithDuplicateEmail() {
        // Arrange
        UserDTO userDTO = UserDTO.builder()
                .firstName("John")
                .lastName("Duplicate")
                .email("duplicate@test.com")
                .password("Password123")
                .role(RoleUtilisateur.ADMIN)
                .build();

        // Act
        userService.createUser(userDTO);

        // Assert
        assertThrows(EmailAlreadyExistsException.class, () -> 
            userService.createUser(userDTO)
        );
    }

    @Test
    @DisplayName("Integration: Should update user successfully")
    void testUpdateUser() {
        // Arrange
        UserDTO userDTO = UserDTO.builder()
                .firstName("Update")
                .lastName("Test")
                .email("update@test.com")
                .password("Password123")
                .role(RoleUtilisateur.RESPONSABLE_LOGISTIQUE)
                .build();

        UserDTO createdUser = userService.createUser(userDTO);

        // Act
        createdUser.setFirstName("Updated Name");
        UserDTO updatedUser = userService.updateUser(createdUser.getId(), createdUser);

        // Assert
        assertEquals("Updated Name", updatedUser.getFirstName());
        assertEquals("update@test.com", updatedUser.getEmail());
    }

    @Test
    @DisplayName("Integration: Should throw UserNotFoundException for non-existent user")
    void testGetNonExistentUser() {
        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> 
            userService.getUser(99999L)
        );
    }

    @Test
    @DisplayName("Integration: Should delete user successfully")
    void testDeleteUser() {
        // Arrange
        UserDTO userDTO = UserDTO.builder()
                .firstName("Delete")
                .lastName("Test")
                .email("delete@test.com")
                .password("Password123")
                .role(RoleUtilisateur.CHEF_PRODUCTION)
                .build();

        UserDTO createdUser = userService.createUser(userDTO);

        // Act
        userService.deleteUser(createdUser.getId());

        // Assert
        assertThrows(UserNotFoundException.class, () -> 
            userService.getUser(createdUser.getId())
        );
    }
}

