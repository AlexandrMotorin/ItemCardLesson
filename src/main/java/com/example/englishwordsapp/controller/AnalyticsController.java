package com.example.englishwordsapp.controller;

import com.example.englishwordsapp.model.WordCard;
import com.example.englishwordsapp.model.WordSet;
import com.example.englishwordsapp.model.UserWordProgress;
import com.example.englishwordsapp.repository.UserWordProgressRepository;
import com.example.englishwordsapp.repository.WordCardRepository;
import com.example.englishwordsapp.repository.WordSetRepository;
import com.example.englishwordsapp.security.CustomUserDetails;
import com.example.englishwordsapp.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final WordSetRepository wordSetRepository;
    private final WordCardRepository wordCardRepository;
    private final UserWordProgressRepository progressRepository;

    @GetMapping
    public String overview(Model model,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        List<AnalyticsService.SetSummary> allSetsStats = analyticsService.getAllSetsStats(userDetails.getId());
        model.addAttribute("setsStats", allSetsStats);

        return "analytics/overview";
    }

    @GetMapping("/sets/{id}")
    public String setDetail(@PathVariable Long id,
                            Model model,
                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        WordSet wordSet = wordSetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WordSet not found with id: " + id));

        AnalyticsService.SetStats setStats = analyticsService.getSetStats(userDetails.getId(), id);
        List<UserWordProgress> progresses = progressRepository.findByUserIdAndWordSetIdOrderByWordCard(userDetails.getId(), id);

        List<WordWithProgress> wordProgressList = new ArrayList<>();
        for (WordCard card : wordSet.getWordCards()) {
            WordWithProgress wp = new WordWithProgress();
            wp.setWordCard(card);
            progresses.stream()
                    .filter(p -> p.getWordCard().getId().equals(card.getId()))
                    .findFirst()
                    .ifPresentOrElse(p -> {
                        wp.setTotalAttempts(p.getTotalAttempts());
                        wp.setCorrectAnswers(p.getCorrectAnswers());
                        wp.setSuccessRate(p.getTotalAttempts() == 0 ? 0.0 :
                                (double) p.getCorrectAnswers() / p.getTotalAttempts() * 100);
                    }, () -> {
                        wp.setTotalAttempts(0);
                        wp.setCorrectAnswers(0);
                        wp.setSuccessRate(0.0);
                    });
            wordProgressList.add(wp);
        }

        model.addAttribute("wordSet", wordSet);
        model.addAttribute("setStats", setStats);
        model.addAttribute("wordProgressList", wordProgressList);

        return "analytics/set-detail";
    }

    @GetMapping("/sets/{id}/words/{wordId}")
    public String wordDetail(@PathVariable Long id,
                             @PathVariable Long wordId,
                             Model model,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        WordSet wordSet = wordSetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WordSet not found with id: " + id));
        WordCard wordCard = wordCardRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("WordCard not found with id: " + wordId));

        AnalyticsService.WordStats wordStats = analyticsService.getWordStats(userDetails.getId(), wordId, id);

        model.addAttribute("wordSet", wordSet);
        model.addAttribute("wordCard", wordCard);
        model.addAttribute("wordStats", wordStats);

        return "analytics/word-detail";
    }

    @lombok.Getter
    @lombok.Setter
    public static class WordWithProgress {
        private WordCard wordCard;
        private int totalAttempts;
        private int correctAnswers;
        private double successRate;
    }
}
