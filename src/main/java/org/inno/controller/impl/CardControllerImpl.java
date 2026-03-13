package org.inno.controller.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.inno.controller.CardController;
import org.inno.service.CardService;
import org.inno.service.dto.CardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/cards")
@RestController
@RequiredArgsConstructor
public class CardControllerImpl implements CardController {
    private final CardService cardService;

    @PostMapping()
    @Override
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CardDto cardDto) {
        var card = cardService.createCard(cardDto);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/{cardId}")
    @Override
    public ResponseEntity<CardDto> getCardById(@PathVariable UUID cardId) {
        var cardById = cardService.getCardById(cardId);
        return ResponseEntity.ok(cardById);
    }

    @PutMapping("/{cardId}")
    @Override
    public ResponseEntity<CardDto> updateCardById(@PathVariable UUID cardId, @Valid @RequestBody CardDto cardDto) {
        var updateCardById = cardService.updateCardById(cardId, cardDto);
        return ResponseEntity.ok(updateCardById);
    }

    @GetMapping()
    @Override
    public ResponseEntity<Page<CardDto>> getAllCards(@RequestParam(required = false) String number,
                                                     @PageableDefault(size = 10) Pageable pageable
    ) {
        var allCards = cardService.getAllCardsWithFilterNumber(number, pageable);
        return ResponseEntity.ok(allCards);
    }

    @GetMapping("/user/{userId}")
    @Override
    public ResponseEntity<List<CardDto>> getAllCardsByUserId(@PathVariable UUID userId) {
        var allCardsByUserId = cardService.getCardsByUserIdWithUser(userId);
        return ResponseEntity.ok(allCardsByUserId);
    }

    @PatchMapping("/active/{cardId}")
    @Override
    public ResponseEntity<Boolean> activateDeactivateCard(@PathVariable UUID cardId) {
        var cardActive = cardService.activateDeactivateCard(cardId);
        return ResponseEntity.ok(cardActive);
    }


    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID cardId) {
        cardService.deleteById(cardId);
        return ResponseEntity.noContent().build();
    }
}
