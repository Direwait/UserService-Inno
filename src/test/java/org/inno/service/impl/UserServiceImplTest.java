package org.inno.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.inno.dao.model.UserModel;
import org.inno.dao.repository.UserRepository;
import org.inno.service.dto.UserDto;
import org.inno.service.mapper.CardMapper;
import org.inno.service.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private UserModel userModel;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userModel = new UserModel();
        userModel.setId(userId);
        userModel.setName("John");
        userModel.setSurname("Doe");
        userModel.setActive(true);
        userModel.setCreatedAt(LocalDateTime.now());
        userModel.setUpdatedAt(LocalDateTime.now());

        userDto = new UserDto();
        userDto.setId(userId);
        userDto.setName("John");
        userDto.setSurname("Doe");
        userDto.setActive(true);
    }

    @Test
    void createUser_ShouldSaveAndReturnUser() {
        when(userMapper.dtoToModel(userDto)).thenReturn(userModel);
        when(userRepository.save(userModel)).thenReturn(userModel);
        when(userMapper.modelToDto(userModel)).thenReturn(userDto);

        UserDto result = userService.createUser(userDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getSurname()).isEqualTo("Doe");

        verify(userMapper).dtoToModel(userDto);
        verify(userRepository).save(userModel);
        verify(userMapper).modelToDto(userModel);
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        UUID userId = UUID.randomUUID();

        UserModel userModel = new UserModel();
        userModel.setId(userId);
        userModel.setEmail("test@mail.com");

        UserDto userDto = new UserDto();
        userDto.setId(userId);
        userDto.setEmail("test@mail.com");

        when(userRepository.findUserWithCardsById(userId)).thenReturn(Optional.of(userModel));
        when(userMapper.modelToDto(userModel)).thenReturn(userDto);

        UserDto result = userService.getUserById(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);

        verify(userRepository).findUserWithCardsById(userId);  // И ЗДЕСЬ ТОЖЕ!
        verify(userMapper).modelToDto(userModel);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findUserWithCardsById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id " + userId);

        verify(userRepository).findUserWithCardsById(userId);
        verifyNoInteractions(userMapper);  // лучше чем verify(userMapper, never()).modelToDto(any())
    }

    @Test
    void updateUserById_WhenUserExists_ShouldUpdateUser() {
        UserDto updateDto = new UserDto();
        updateDto.setName("Jane");
        updateDto.setSurname("Smith");

        LocalDateTime fixedToday = LocalDateTime.of(2026, 3, 11, 0,0);
        LocalDateTime beforeUpdate = LocalDateTime.of(2026, 3, 10, 0,0);

        userModel.setUpdatedAt(beforeUpdate);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userModel));

        try (MockedStatic<LocalDateTime> mockedLocalDate = mockStatic(LocalDateTime.class)) {
            mockedLocalDate.when(LocalDateTime::now).thenReturn(fixedToday);

            userService.updateUserById(userId, updateDto);

            verify(userMapper).updateFromDto(updateDto, userModel);
            assertThat(userModel.getUpdatedAt()).isEqualTo(fixedToday);
            verify(userRepository).findById(userId);
        }
    }

    @Test
    void updateUserById_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserById(userId, userDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id " + userId);

        verify(userRepository).findById(userId);
        verify(userMapper, never()).updateFromDto(any(), any());
    }

    @Test
    void activateDeactivateUser_WhenUserIsActive_ShouldDeactivate() {
        userModel.setActive(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userModel));


        boolean result = userService.activateDeactivateUser(userId);  //注意: метод с большой буквы


        assertThat(result).isFalse();
        assertThat(userModel.isActive()).isFalse();
        verify(userRepository).findById(userId);
    }

    @Test
    void activateDeactivateUser_WhenUserIsInactive_ShouldActivate() {
        userModel.setActive(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userModel));

        boolean result = userService.activateDeactivateUser(userId);

        assertThat(result).isTrue();
        assertThat(userModel.isActive()).isTrue();
        verify(userRepository).findById(userId);
    }

    @Test
    void activateDeactivateUser_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.activateDeactivateUser(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id " + userId);

        verify(userRepository).findById(userId);
    }

    @Test
    void getAllUsers_WithSearchTerm_ShouldReturnFilteredUsers() {
        String searchTerm = "John";
        Pageable pageable = PageRequest.of(0, 10);
        Specification<UserModel> spec = mock(Specification.class);


        try (var mockedStatic = mockStatic(UserRepository.class)) {
            mockedStatic.when(() -> UserRepository.filterByNameOrSurname(searchTerm))
                    .thenReturn(spec);

            Page<UserModel> userPage = new PageImpl<>(List.of(userModel));
            when(userRepository.findAll(spec, pageable)).thenReturn(userPage);
            when(userMapper.modelToDto(userModel)).thenReturn(userDto);


            Page<UserDto> result = userService.getAllUsers(searchTerm, pageable);


            assertThat(result).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(userId);

            mockedStatic.verify(() -> UserRepository.filterByNameOrSurname(searchTerm));
            verify(userRepository).findAll(spec, pageable);
            verify(userMapper).modelToDto(userModel);
        }
    }

    @Test
    void getAllUsers_WithNullSearchTerm_ShouldReturnAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Specification<UserModel> spec = mock(Specification.class);

        try (var mockedStatic = mockStatic(UserRepository.class)) {
            mockedStatic.when(() -> UserRepository.filterByNameOrSurname(null))
                    .thenReturn(spec);

            Page<UserModel> userPage = new PageImpl<>(List.of(userModel));
            when(userRepository.findAll(spec, pageable)).thenReturn(userPage);
            when(userMapper.modelToDto(userModel)).thenReturn(userDto);

            Page<UserDto> result = userService.getAllUsers(null, pageable);

            assertThat(result).hasSize(1);
            mockedStatic.verify(() -> UserRepository.filterByNameOrSurname(null));
            verify(userRepository).findAll(spec, pageable);
        }
    }

    @Test
    void getAllUsers_WithEmptySearchTerm_ShouldReturnAllUsers() {
        String searchTerm = "   ";
        Pageable pageable = PageRequest.of(0, 10);
        Specification<UserModel> spec = mock(Specification.class);

        try (var mockedStatic = mockStatic(UserRepository.class)) {
            mockedStatic.when(() -> UserRepository.filterByNameOrSurname(searchTerm))
                    .thenReturn(spec);

            Page<UserModel> userPage = new PageImpl<>(List.of(userModel));
            when(userRepository.findAll(spec, pageable)).thenReturn(userPage);
            when(userMapper.modelToDto(userModel)).thenReturn(userDto);

            Page<UserDto> result = userService.getAllUsers(searchTerm, pageable);

            assertThat(result).hasSize(1);
            mockedStatic.verify(() -> UserRepository.filterByNameOrSurname(searchTerm));
            verify(userRepository).findAll(spec, pageable);
        }
    }

    @Test
    void deleteById_WhenUserExists_ShouldDeleteUser() {
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteById(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteById_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id " + userId);

        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(any());
    }
}