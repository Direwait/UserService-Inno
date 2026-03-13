package org.inno.controller;

import org.inno.service.dto.CardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface CardController {

    ResponseEntity<CardDto> createCard(CardDto cardDto);

    ResponseEntity<CardDto> getCardById(UUID cardId);

    ResponseEntity<CardDto> updateCardById(UUID cardId, CardDto cardDto);

    ResponseEntity<Page<CardDto>> getAllCards(String number, Pageable pageable);

    ResponseEntity<List<CardDto>> getAllCardsByUserId(UUID userId);

    ResponseEntity<Boolean> activateDeactivateCard(UUID cardId);

    ResponseEntity<Void> deleteById(UUID cardId);
}
