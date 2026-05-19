package com.example.englishwordsapp.controller;

import com.example.englishwordsapp.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/sets";
        }
        return "landing";
    }
}
