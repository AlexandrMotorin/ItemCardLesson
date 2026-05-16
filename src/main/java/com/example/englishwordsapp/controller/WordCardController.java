package com.example.englishwordsapp.controller;

import com.example.englishwordsapp.model.WordCard;
import com.example.englishwordsapp.service.WordCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cards")
@RequiredArgsConstructor
public class WordCardController {

    private final WordCardService wordCardService;

    @GetMapping
    public String listCards(Model model) {
        model.addAttribute("cards", wordCardService.getAllCards());
        return "index";
    }

    @GetMapping("/new")
    public String showNewCardForm(Model model) {
        WordCard wordCard = new WordCard();
        model.addAttribute("wordCard", wordCard);
        return "fragments/card-form";
    }

    @PostMapping
    public String createCard(@Valid @ModelAttribute("wordCard") WordCard wordCard,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult);
            return "fragments/card-form";
        }
        wordCardService.createCard(wordCard);
        redirectAttributes.addFlashAttribute("successMessage", "Card created successfully!");
        return "redirect:/cards";
    }

    @GetMapping("/{id}")
    public String viewCard(@PathVariable Long id, Model model) {
        return wordCardService.getCardById(id)
                .map(card -> {
                    model.addAttribute("card", card);
                    return "fragments/card-detail";
                })
                .orElse("redirect:/cards");
    }

    @GetMapping("/{id}/edit")
    public String showEditCardForm(@PathVariable Long id, Model model) {
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

    @GetMapping("/random")
    public String randomCard(Model model) {
        WordCard card = wordCardService.getRandomCard();
        if (card == null) {
            return "redirect:/cards/new";
        }
        model.addAttribute("card", card);
        return "fragments/card-detail";
    }

    @GetMapping("/search")
    public String searchCards(@RequestParam String query, Model model) {
        model.addAttribute("cards", wordCardService.searchCards(query));
        model.addAttribute("query", query);
        return "fragments/cards-list";
    }
}
