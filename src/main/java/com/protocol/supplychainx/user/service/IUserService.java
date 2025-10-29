package com.protocol.supplychainx.user.service;

import com.protocol.supplychainx.user.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(Long id, UserDTO userDTO);
    UserDTO getUser(Long id);
    UserDTO getUserByEmail(String email);
    Page<UserDTO> getUsers(Pageable pageable);
    Page<UserDTO> searchUsersByName(String name, Pageable pageable);
    void deleteUser(Long id);
}
