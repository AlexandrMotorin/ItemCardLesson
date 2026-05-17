package com.example.englishwordsapp.controller;

import com.example.englishwordsapp.model.UserCard;
import com.example.englishwordsapp.model.WordCard;
import com.example.englishwordsapp.model.WordSet;
import com.example.englishwordsapp.security.CustomUserDetails;
import com.example.englishwordsapp.service.WordCardService;
import com.example.englishwordsapp.service.WordSetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cards")
@RequiredArgsConstructor
public class WordCardController {

    private final WordCardService wordCardService;
    private final WordSetService wordSetService;

    @GetMapping
    public String listCards(Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long setId) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Long userId = userDetails.getId();

        List<WordCard> userCards;
        if (setId != null) {
            userCards = wordCardService.getUserCardsFromSet(setId);
        } else {
            userCards = wordCardService.getUserCards(userId);
        }

        List<UserCard> userCardEntities = wordCardService.getUserCardEntities(userId);
        Map<Long, UserCard> userCardMap = userCardEntities.stream()
                .collect(Collectors.toMap(uc -> uc.getWordCard().getId(), Function.identity()));

        List<WordSet> userSets = wordSetService.getUserSets(userId);
        model.addAttribute("cards", userCards);
        model.addAttribute("userCardMap", userCardMap);
        model.addAttribute("userSets", userSets);
        model.addAttribute("selectedSetId", setId);
        return "index";
    }

    @GetMapping("/new")
    public String showAddCardForm(Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        if (!model.containsAttribute("wordCard")) {
            model.addAttribute("wordCard", new WordCard());
        }
        List<WordSet> userSets = wordSetService.getUserSets(userDetails.getId());
        model.addAttribute("userSets", userSets);
        return "fragments/card-form";
    }

    @PostMapping
    public String addCardToCollection(@Valid @ModelAttribute("wordCard") WordCard wordCard,
            BindingResult bindingResult,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long setId,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Long userId = userDetails.getId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult);
            List<WordSet> userSets = wordSetService.getUserSets(userId);
            model.addAttribute("userSets", userSets);
            return "fragments/card-form";
        }

        if (wordCard.getId() != null) {
            if (setId != null) {
                wordSetService.addWordToSet(setId, wordCard.getId(), userId);
            } else {
                wordCardService.addCardToUserCollection(userId, wordCard.getId());
            }
            redirectAttributes.addFlashAttribute("successMessage", "Word added to your collection!");
            return "redirect:/cards";
        }

        WordCard savedCard = wordCardService.createCardAndAddToUserCollection(userId, wordCard);
        redirectAttributes.addFlashAttribute("successMessage", "New word created and added to your collection!");
        return "redirect:/cards";
    }

    @GetMapping("/{id}")
    public String viewCard(@PathVariable Long id, Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Long userId = userDetails.getId();

        return wordCardService.getCardById(id)
                .map(card -> {
                    model.addAttribute("card", card);
                    model.addAttribute("inCollection", wordCardService.isCardInUserCollection(userId, id));
                    wordCardService.getUserCard(userId, id).ifPresent(uc ->
                            model.addAttribute("userCard", uc)
                    );
                    return "fragments/card-detail";
                })
                .orElse("redirect:/cards");
    }

    @PostMapping("/{id}/add")
    public String addToCollection(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        wordCardService.addCardToUserCollection(userDetails.getId(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Word added to your collection!");
        return "redirect:/cards";
    }

    @PostMapping("/{id}/remove")
    public String removeFromCollection(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        wordCardService.removeCardFromUserCollection(userDetails.getId(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Word removed from your collection!");
        return "redirect:/cards";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
            @RequestParam UserCard.StudyStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        wordCardService.updateStudyStatus(userDetails.getId(), id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Study status updated!");
        return "redirect:/cards/" + id;
    }

    @GetMapping("/random")
    public String randomCard(Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Long userId = userDetails.getId();
        List<WordCard> userCards = wordCardService.getUserCards(userId);
        if (userCards.isEmpty()) {
            return "redirect:/cards/new";
        }
        WordCard card = userCards.get((int) (Math.random() * userCards.size()));
        model.addAttribute("card", card);
        model.addAttribute("inCollection", true);
        wordCardService.getUserCard(userId, card.getId()).ifPresent(uc ->
                model.addAttribute("userCard", uc)
        );
        return "fragments/card-detail";
    }

    @GetMapping("/search-global")
    @ResponseBody
    public List<WordCard> searchGlobalPool(@RequestParam String query) {
        return wordCardService.searchGlobalPool(query);
    }

    @GetMapping("/{id}/edit")
    public String showEditCardForm(@PathVariable Long id, Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        return wordCardService.getCardById(id)
                .map(card -> {
                    model.addAttribute("wordCard", card);
                    return "fragments/card-form";
                })
                .orElse("redirect:/cards");
    }

    @PostMapping("/{id}")
    public String updateCard(@PathVariable Long id,
            @Valid @ModelAttribute("wordCard") WordCard wordCard,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult);
            return "fragments/card-form";
        }
        wordCardService.updateCard(id, wordCard);
        redirectAttributes.addFlashAttribute("successMessage", "Card updated successfully!");
        return "redirect:/cards";
    }

    @PostMapping("/{id}/delete")
    public String deleteCard(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        wordCardService.deleteCard(id);
        redirectAttributes.addFlashAttribute("successMessage", "Card deleted successfully!");
        return "redirect:/cards";
    }
}
