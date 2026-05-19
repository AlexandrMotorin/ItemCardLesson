package com.example.englishwordsapp.controller;

import com.example.englishwordsapp.dto.QuestionDto;
import com.example.englishwordsapp.model.Direction;
import com.example.englishwordsapp.model.StudyResult;
import com.example.englishwordsapp.model.StudySession;
import com.example.englishwordsapp.model.WordCard;
import com.example.englishwordsapp.model.WordSet;
import com.example.englishwordsapp.security.CustomUserDetails;
import com.example.englishwordsapp.service.StudyService;
import com.example.englishwordsapp.service.WordSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.expression.Numbers;

import java.util.List;

@Controller
@RequestMapping("/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final WordSetService wordSetService;

    @GetMapping
    public String chooseSets(Model model,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        List<WordSet> availableSets = wordSetService.getAvailableSets(userDetails.getId());
        List<WordSet> subscribedSets = wordSetService.getSubscribedSets(userDetails.getId());

        model.addAttribute("availableSets", availableSets);
        model.addAttribute("subscribedSets", subscribedSets);

        return "study/choose";
    }

    @PostMapping("/start")
    public String startSession(@RequestParam(value = "setIds", required = false) List<Long> setIds,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        if (setIds == null || setIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select at least one word set");
            return "redirect:/study";
        }

        StudySession session = studyService.startSession(userDetails.getId(), setIds);
        return "redirect:/study/session/" + session.getId();
    }

    @GetMapping("/session/{id}")
    public String showSession(@PathVariable Long id,
                              Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        StudySession session = studyService.getSession(id);

        // Verify the session belongs to the current user
        if (!session.getUser().getId().equals(userDetails.getId())) {
            return "redirect:/study";
        }

        // Check if session is completed
        if (session.getCurrentIndex() >= session.getWordCount()) {
            return "redirect:/study/session/" + id + "/results";
        }

        QuestionDto question = studyService.getCurrentQuestionInfo(id);
        model.addAttribute("studySession", session);
        model.addAttribute("question", question);
        model.addAttribute("word", question.getWordCard());
        model.addAttribute("currentIndex", question.getCurrentIndex() + 1);
        model.addAttribute("totalWords", question.getTotalWords());
        model.addAttribute("exerciseType", question.getExerciseType().name());
        model.addAttribute("direction", question.getDirection().name());
        if (question.getChoices() != null) {
            model.addAttribute("choices", question.getChoices());
        }

        return "study/session";
    }

    @PostMapping("/session/{id}/answer")
    public String submitAnswer(@PathVariable Long id,
                               @RequestParam("wordCardId") Long wordCardId,
                               @RequestParam(value = "answer", required = false) String answer,
                               @RequestParam(value = "direction", required = false) String directionStr,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        StudySession session = studyService.getSession(id);
        if (!session.getUser().getId().equals(userDetails.getId())) {
            return "redirect:/study";
        }

        Direction direction = directionStr != null ? Direction.valueOf(directionStr) : Direction.EN_TO_RU;
        StudyResult result = studyService.submitAnswer(id, wordCardId, answer, direction);

        redirectAttributes.addFlashAttribute("lastResult", result.isCorrect());

        // Check if session is complete
        if (session.getCurrentIndex() >= session.getWordCount() - 1) {
            studyService.endSession(id);
            return "redirect:/study/session/" + id + "/results";
        }

        return "redirect:/study/session/" + id;
    }

    @GetMapping("/session/{id}/next")
    public String nextWord(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        StudySession session = studyService.getSession(id);
        if (!session.getUser().getId().equals(userDetails.getId())) {
            return "redirect:/study";
        }

        WordCard nextWord = studyService.getNextWord(id);
        if (nextWord == null) {
            studyService.endSession(id);
            return "redirect:/study/session/" + id + "/results";
        }

        return "redirect:/study/session/" + id;
    }

    @GetMapping("/session/{id}/results")
    public String showResults(@PathVariable Long id,
                              Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        StudySession session = studyService.getSession(id);
        if (!session.getUser().getId().equals(userDetails.getId())) {
            return "redirect:/study";
        }

        List<StudyResult> results = studyService.getSessionResults(id);
        long correctCount = results.stream().filter(StudyResult::isCorrect).count();
        long incorrectCount = results.size() - correctCount;
        double percentage = results.isEmpty() ? 0.0 : (correctCount * 100.0 / results.size());

        List<StudyResult> incorrectResults = results.stream()
                .filter(r -> !r.isCorrect())
                .toList();

        model.addAttribute("studySession", session);
        model.addAttribute("results", results);
        model.addAttribute("incorrectResults", incorrectResults);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("incorrectCount", incorrectCount);
        model.addAttribute("percentage", percentage);
        model.addAttribute("percentageFormatted", String.format("%.1f", percentage));

        return "study/results";
    }
}
