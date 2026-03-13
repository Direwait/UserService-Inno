package org.inno.service;

import org.inno.service.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto getUserById(UUID userId);

    UserDto updateUserById(UUID userId, UserDto userDto);

    boolean activateDeactivateUser(UUID userId);

    Page<UserDto> getAllUsers(String searchTerm, Pageable pageable);

    void deleteById(UUID userId);
}
