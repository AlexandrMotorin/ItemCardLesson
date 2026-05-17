package com.example.englishwordsapp.controller;

import com.example.englishwordsapp.model.UserCard;
import com.example.englishwordsapp.model.WordCard;
import com.example.englishwordsapp.security.CustomUserDetails;
import com.example.englishwordsapp.service.WordCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class WordCardRestController {

    private final WordCardService wordCardService;

    @GetMapping
    public List<WordCard> getAllCards() {
        return wordCardService.getAllCards();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WordCard> getCardById(@PathVariable Long id) {
        return wordCardService.getCardById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public WordCard createCard(@Valid @RequestBody WordCard wordCard) {
        return wordCardService.createCard(wordCard);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WordCard> updateCard(@PathVariable Long id, @Valid @RequestBody WordCard wordCard) {
        try {
            return ResponseEntity.ok(wordCardService.updateCard(id, wordCard));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        wordCardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/random")
    public WordCard getRandomCard() {
        return wordCardService.getRandomCard();
    }

    @GetMapping("/search")
    public List<WordCard> searchCards(@RequestParam String query) {
        return wordCardService.searchCards(query);
    }

    @GetMapping("/difficulty/{level}")
    public List<WordCard> getCardsByDifficulty(@PathVariable WordCard.DifficultyLevel level) {
        return wordCardService.getCardsByDifficulty(level);
    }

    // ========== User collection endpoints ==========

    @GetMapping("/my")
    public List<WordCard> getMyCards(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return wordCardService.getUserCards(userDetails.getId());
    }

    @GetMapping("/my/ids")
    public Set<Long> getMyCardIds(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return wordCardService.getUserCardIds(userDetails.getId());
    }

    @PostMapping("/{id}/add-to-collection")
    public ResponseEntity<UserCard> addToCollection(@PathVariable Long id,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            UserCard userCard = wordCardService.addCardToUserCollection(userDetails.getId(), id);
            return ResponseEntity.ok(userCard);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/remove-from-collection")
    public ResponseEntity<Void> removeFromCollection(@PathVariable Long id,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        wordCardService.removeCardFromUserCollection(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/study-status")
    public ResponseEntity<UserCard> updateStudyStatus(@PathVariable Long id,
                                                      @RequestParam UserCard.StudyStatus status,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            UserCard userCard = wordCardService.updateStudyStatus(userDetails.getId(), id, status);
            return ResponseEntity.ok(userCard);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search-global")
    public List<WordCard> searchGlobalPool(@RequestParam String query) {
        return wordCardService.searchGlobalPool(query);
    }
}
