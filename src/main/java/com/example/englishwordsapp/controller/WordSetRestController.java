package com.example.englishwordsapp.controller;

import com.example.englishwordsapp.model.WordCard;
import com.example.englishwordsapp.model.WordSet;
import com.example.englishwordsapp.security.CustomUserDetails;
import com.example.englishwordsapp.service.WordSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sets")
@RequiredArgsConstructor
public class WordSetRestController {

    private final WordSetService wordSetService;

    @GetMapping
    public ResponseEntity<?> getAvailableSets(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(wordSetService.getAvailableSets(userDetails.getId()));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMySets(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(wordSetService.getUserSets(userDetails.getId()));
    }

    @GetMapping("/system")
    public List<WordSet> getSystemSets() {
        return wordSetService.getSystemSets();
    }

    @PostMapping
    public ResponseEntity<?> createSet(@RequestBody Map<String, String> body,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(wordSetService.createSet(userDetails.getId(),
                body.get("name"),
                body.get("description")));
    }

    @GetMapping("/{id}/words")
    public List<WordCard> getSetWords(@PathVariable Long id) {
        return wordSetService.getSetWords(id);
    }

    @PostMapping("/{id}/subscribe")
    public ResponseEntity<Void> subscribe(@PathVariable Long id,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        wordSetService.subscribeToSet(userDetails.getId(), id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/subscribe")
    public ResponseEntity<Void> unsubscribe(@PathVariable Long id,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        wordSetService.unsubscribeFromSet(userDetails.getId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/fork")
    public ResponseEntity<?> fork(@PathVariable Long id,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        WordSet forked = wordSetService.forkSet(userDetails.getId(), id);
        return ResponseEntity.ok(forked);
    }
}