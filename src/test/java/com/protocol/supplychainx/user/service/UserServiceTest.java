package com.protocol.supplychainx.user.service;

import com.protocol.supplychainx.common.enums.RoleUtilisateur;
import com.protocol.supplychainx.common.exceptions.user.EmailAlreadyExistsException;
import com.protocol.supplychainx.common.exceptions.user.UserNotFoundException;
import com.protocol.supplychainx.user.dto.UserDTO;
import com.protocol.supplychainx.user.entity.User;
import com.protocol.supplychainx.user.mapper.UserMapper;
import com.protocol.supplychainx.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserDTO userDTO;
    private User user;

    @BeforeEach
    void setUp() {
        userDTO = UserDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("Password123")
                .role(RoleUtilisateur.ADMIN)
                .build();

        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .role(RoleUtilisateur.ADMIN)
                .build();
    }

    @Test
    @DisplayName("Should create user successfully")
    void testCreateUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(UserDTO.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        // Act
        UserDTO result = userService.createUser(userDTO);

        // Assert
        assertNotNull(result);
        assertEquals(userDTO.getEmail(), result.getEmail());
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email exists")
    void testCreateUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(userDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUser_Success() {
        // Arrange
        UserDTO updateDTO = UserDTO.builder()
                .firstName("John Updated")
                .lastName("Doe Updated")
                .email("john.doe@example.com")  // Same email
                .password("NewPassword123")
                .role(RoleUtilisateur.CHEF_PRODUCTION)
                .build();
        
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        // Act
        UserDTO result = userService.updateUser(1L, updateDTO);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when updating non-existent user")
    void testUpdateUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, userDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUser_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        // Act
        UserDTO result = userService.getUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(userDTO.getId(), result.getId());
        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found by ID")
    void testGetUser_NotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUser(1L));
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void testGetUserByEmail_Success() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        // Act
        UserDTO result = userService.getUserByEmail("john.doe@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(userDTO.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findByEmail(anyString());
    }

    @Test
    @DisplayName("Should get all users with pagination")
    void testGetUsers_Success() {
        // Arrange
        Page<User> userPage = new PageImpl<>(Arrays.asList(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        // Act
        Page<UserDTO> result = userService.getUsers(PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should search users by name successfully")
    void testSearchUsersByName_Success() {
        // Arrange
        Page<User> userPage = new PageImpl<>(Arrays.asList(user));
        when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                anyString(), anyString(), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        // Act
        Page<UserDTO> result = userService.searchUsersByName("John", PageRequest.of(0, 10));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                anyString(), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser_Success() {
        // Arrange
        when(userRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(userRepository).deleteById(anyLong());

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).existsById(anyLong());
        verify(userRepository, times(1)).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
    void testDeleteUser_UserNotFound() {
        // Arrange
        when(userRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).deleteById(anyLong());
    }
}
