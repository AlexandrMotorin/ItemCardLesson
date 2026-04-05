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
        return "cards-list.jte";
    }

    @GetMapping("/new")
    public String showNewCardForm(Model model) {
        model.addAttribute("wordCard", new WordCard());
        return "card-form.jte";
    }

    @PostMapping
    public String createCard(@Valid @ModelAttribute WordCard wordCard, 
                             BindingResult bindingResult, 
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "card-form.jte";
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
                    return "card-detail.jte";
                })
                .orElse("redirect:/cards");
    }

    @GetMapping("/{id}/edit")
    public String showEditCardForm(@PathVariable Long id, Model model) {
        return wordCardService.getCardById(id)
                .map(card -> {
                    model.addAttribute("wordCard", card);
                    return "card-form.jte";
                })
                .orElse("redirect:/cards");
    }

    @PostMapping("/{id}")
    public String updateCard(@PathVariable Long id, 
                             @Valid @ModelAttribute WordCard wordCard, 
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "card-form.jte";
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
        return "card-detail.jte";
    }

    @GetMapping("/search")
    public String searchCards(@RequestParam String query, Model model) {
        model.addAttribute("cards", wordCardService.searchCards(query));
        model.addAttribute("query", query);
        return "cards-list.jte";
    }
}
