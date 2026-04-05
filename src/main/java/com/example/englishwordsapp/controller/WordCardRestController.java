package com.example.englishwordsapp.controller;

import com.example.englishwordsapp.model.WordCard;
import com.example.englishwordsapp.service.WordCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
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
    public WordCard createCard(@RequestBody WordCard wordCard) {
        return wordCardService.createCard(wordCard);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WordCard> updateCard(@PathVariable Long id, @RequestBody WordCard wordCard) {
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
}
