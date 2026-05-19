package com.example.englishwordsapp.controller;

import com.example.englishwordsapp.model.WordSet;
import com.example.englishwordsapp.security.CustomUserDetails;
import com.example.englishwordsapp.service.WordSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/sets")
@RequiredArgsConstructor
public class WordSetController {

    private final WordSetService wordSetService;

    @GetMapping
    public String listSets(Model model,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        Long userId = userDetails.getId();

        List<WordSet> availableSets = wordSetService.getAvailableSets(userId);
        List<WordSet> mySets = wordSetService.getUserSets(userId);
        List<WordSet> subscribedSets = wordSetService.getSubscribedSets(userId);

        model.addAttribute("availableSets", availableSets);
        model.addAttribute("mySets", mySets);
        model.addAttribute("subscribedSets", subscribedSets);
        return "fragments/sets-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("wordSet", new WordSet());
        return "fragments/set-form";
    }

    @PostMapping("/create")
    public String createSet(@RequestParam String name,
                            @RequestParam(required = false) String description,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        wordSetService.createSet(userDetails.getId(), name, description);
        redirectAttributes.addFlashAttribute("successMessage", "Set created!");
        return "redirect:/sets";
    }

    @GetMapping("/{id}")
    public String viewSet(@PathVariable Long id,
                          Model model,
                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        WordSet wordSet = wordSetService.getAvailableSets(userDetails.getId()).stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (wordSet == null) return "redirect:/sets";

        Long userId = userDetails.getId();
        boolean isOwner = wordSet.getOwner() != null && wordSet.getOwner().getId().equals(userId);

        model.addAttribute("set", wordSet);
        model.addAttribute("words", wordSet.getWordCards());
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isSubscribed", wordSetService.isSubscribed(userId, id));
        return "fragments/set-detail";
    }

    @PostMapping("/{id}/subscribe")
    public String subscribe(@PathVariable Long id,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        wordSetService.subscribeToSet(userDetails.getId(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Subscribed!");
        return "redirect:/sets";
    }

    @PostMapping("/{id}/unsubscribe")
    public String unsubscribe(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        wordSetService.unsubscribeFromSet(userDetails.getId(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Unsubscribed!");
        return "redirect:/sets";
    }

    @PostMapping("/{id}/fork")
    public String fork(@PathVariable Long id,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        wordSetService.forkSet(userDetails.getId(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Set forked!");
        return "redirect:/sets";
    }
}