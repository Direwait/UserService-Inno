package org.inno.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.inno.dao.model.CardModel;
import org.inno.dao.repository.CardRepository;
import org.inno.exception.CardLimitException;
import org.inno.service.CardService;
import org.inno.service.dto.CardDto;
import org.inno.service.mapper.CardMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;


    @Override
    @Transactional
    public CardDto createCard(CardDto cardDto) {
        int cardsCount = cardRepository.countByUserId(cardDto.getUserId());

        if (cardsCount >= 5) {
            throw new CardLimitException("User cannot have more than 5 cards");
        }
        var cardModel = cardMapper.dtoToModel(cardDto);
        var save = cardRepository.save(cardModel);
        return cardMapper.modelToDto(save);
    }

    @Override
    @Cacheable(value = "card", key = "#cardId")
    public CardDto getCardById(UUID cardId) {
        var cardModel = cardRepository.findById(cardId).orElseThrow(
                () -> new EntityNotFoundException("Card not found with id " + cardId)
        );
        return cardMapper.modelToDto(cardModel);
    }

    @Override
    @CachePut(value = "card", key = "#cardId")
    @Transactional
    public CardDto updateCardById(UUID cardId, CardDto cardDto) {
        var cardModel = cardRepository.findById(cardId).orElseThrow(
                () -> new EntityNotFoundException("Card not found with id " + cardId)
        );
        cardMapper.updateFromDto(cardDto, cardModel);
        cardModel.setUpdatedAt(LocalDateTime.now());
        return cardMapper.modelToDto(cardModel);
    }

    @Override
    @Transactional
    public boolean activateDeactivateCard(UUID cardId) {
        var cardModel = cardRepository.findById(cardId).orElseThrow(
                () -> new EntityNotFoundException("Card not found with id " + cardId)
        );
        if (cardModel.isActive()) {
            cardModel.setActive(false);
            log.info("User with id {} was Deactivated", cardId);
        }
        else {
            cardModel.setActive(true);
            log.info("User with id {} was Activated", cardId);
        }
        return cardModel.isActive();
    }

    @Override
    @Cacheable(value = "cards", key = "#userId")
    public List<CardDto> getAllCardsByUserId(UUID userId) {
        return cardRepository.findAllByUser_Id(userId).stream()
                .map(cardMapper::modelToDto)
                .toList();
    }

    @Override
    public List<CardDto> getCardsByUserIdWithUser(UUID userId) {
        return cardRepository.findAllCardsByUserIdWithUser(userId).stream()
                .map(cardMapper::modelToDto)
                .toList();
    }

    @Override
    public Page<CardDto> getAllCardsWithFilterNumber(@RequestParam String cardNumber, Pageable pageable) {
        Specification<CardModel> spec = CardRepository.filterByNumber(cardNumber);

        return cardRepository.findAll(spec, pageable)
                .map(cardMapper::modelToDto);
    }

    @Override
    @CacheEvict(value = "card", key = "#cardId") // очищаем по ключу
    @Transactional
    public void deleteById(UUID cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new EntityNotFoundException("Card not found with id " + cardId);
        }
        cardRepository.deleteById(cardId);
    }
}
