package org.inno.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.inno.dao.model.CardModel;
import org.inno.dao.model.UserModel;
import org.inno.dao.repository.CardRepository;
import org.inno.service.dto.CardDto;
import org.inno.service.mapper.CardMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {
    @Mock
    private CardRepository cardRepository;  // Исправлено имя

    @Mock
    private CardMapper cardMapper;  // Исправлено имя

    @InjectMocks
    private CardServiceImpl cardService;

    private UUID cardId;
    private UUID userId;
    private CardModel cardModel;
    private CardDto cardDto;

    @BeforeEach
    void setUp() {
        cardId = UUID.randomUUID();
        userId = UUID.randomUUID();

        UserModel userModel = new UserModel();
        userModel.setId(userId);
        userModel.setName("Test User");

        cardModel = new CardModel();
        cardModel.setId(cardId);
        cardModel.setNumber("1234567890123456");
        cardModel.setHolder("Test Holder");
        cardModel.setActive(true);
        cardModel.setUser(userModel);
        cardModel.setCreatedAt(LocalDateTime.now());
        cardModel.setUpdatedAt(LocalDateTime.now());

        cardDto = new CardDto();
        cardDto.setId(cardId);
        cardDto.setNumber("1234567890123456");
        cardDto.setHolder("Test Holder");
        cardDto.setActive(true);
        cardDto.setUserId(userId);
    }

    @Test
    void createCard_ShouldSaveAndReturnCard() {
        when(cardMapper.dtoToModel(cardDto)).thenReturn(cardModel);
        when(cardRepository.save(cardModel)).thenReturn(cardModel);
        when(cardMapper.modelToDto(cardModel)).thenReturn(cardDto);

        CardDto result = cardService.createCard(cardDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(cardId);
        verify(cardRepository).save(cardModel);
    }

    @Test
    void getCardById_WhenCardExists_ShouldReturnCard() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(cardModel));
        when(cardMapper.modelToDto(cardModel)).thenReturn(cardDto);

        CardDto result = cardService.getCardById(cardId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(cardId);
        verify(cardRepository).findById(cardId);
    }

    @Test
    void getCardById_WhenCardDoesNotExist_ShouldThrowException() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(cardId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Card not found with id " + cardId);
    }

    @Test
    void updateCardById_WhenCardExists_ShouldUpdateCard() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(cardModel));

        cardService.updateCardById(cardId, cardDto);

        verify(cardMapper).updateFromDto(cardDto, cardModel);
        verify(cardRepository).findById(cardId);
        assertThat(cardModel.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateCardById_WhenCardDoesNotExist_ShouldThrowException() {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.updateCardById(cardId, cardDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Card not found with id " + cardId);
    }

    @Test
    void activateDeactivateCard_WhenCardIsActive_ShouldDeactivate() {
        cardModel.setActive(true);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(cardModel));


        boolean result = cardService.activateDeactivateCard(cardId);

        assertThat(result).isFalse();
        assertThat(cardModel.isActive()).isFalse();
    }

    @Test
    void activateDeactivateCard_WhenCardIsInactive_ShouldActivate() {
        cardModel.setActive(false);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(cardModel));

        boolean result = cardService.activateDeactivateCard(cardId);

        assertThat(result).isTrue();
        assertThat(cardModel.isActive()).isTrue();
    }

    @Test
    void activateDeactivateCard_WhenCardDoesNotExist_ShouldThrowException() {

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.activateDeactivateCard(cardId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Card not found with id " + cardId);
    }

    @Test
    void getAllCardsByUserId_ShouldReturnCardsList() {
        List<CardModel> cardModels = List.of(cardModel);
        when(cardRepository.findAllByUser_Id(userId)).thenReturn(cardModels);
        when(cardMapper.modelToDto(cardModel)).thenReturn(cardDto);

        List<CardDto> result = cardService.getAllCardsByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(cardId);
        verify(cardRepository).findAllByUser_Id(userId);
    }

    @Test
    void getAllCardsByUserId_WhenNoCards_ShouldReturnEmptyList() {
        when(cardRepository.findAllByUser_Id(userId)).thenReturn(List.of());

        List<CardDto> result = cardService.getAllCardsByUserId(userId);

        assertThat(result).isEmpty();
        verify(cardRepository).findAllByUser_Id(userId);
        verify(cardMapper, never()).modelToDto(any());
    }

    @Test
    void getCardsByUserIdWithUser_ShouldReturnCardsWithUser() {
        List<CardModel> cardModels = List.of(cardModel);
        when(cardRepository.findAllCardsByUserIdWithUser(userId)).thenReturn(cardModels);
        when(cardMapper.modelToDto(cardModel)).thenReturn(cardDto);

        List<CardDto> result = cardService.getCardsByUserIdWithUser(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(cardId);
        verify(cardRepository).findAllCardsByUserIdWithUser(userId);
    }

    @Test
    void deleteById_WhenCardExists_ShouldDeleteCard() {
        when(cardRepository.existsById(cardId)).thenReturn(true);

        cardService.deleteById(cardId);

        verify(cardRepository).existsById(cardId);
        verify(cardRepository).deleteById(cardId);
    }

    @Test
    void deleteById_WhenCardDoesNotExist_ShouldThrowException() {
        when(cardRepository.existsById(cardId)).thenReturn(false);

        assertThatThrownBy(() -> cardService.deleteById(cardId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Card not found with id " + cardId);

        verify(cardRepository).existsById(cardId);
        verify(cardRepository, never()).deleteById(any());
    }
}