package org.inno.controller;

import org.inno.service.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface UserController {

    ResponseEntity<UserDto> createUser(UserDto userId);

    ResponseEntity<UserDto> getUserById(UUID userId);

    ResponseEntity<UserDto> updateUserById(UUID userId, UserDto userDto);

    ResponseEntity<Page<UserDto>> getAllUsers(String searchTerm, Pageable pageable);

    ResponseEntity<Boolean> activateDeactivateCard(UUID cardId);

    ResponseEntity<Void> deleteById(UUID userId);
}
