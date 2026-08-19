package com.example.opac.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication != null) {
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_LIBRARIAN")) {
                return "redirect:/admin/books";
            }
        }
        return "redirect:/books";
    }

    @GetMapping("/admin/books")
    public String adminBooks() {
        return "admin/books";
    }

    @GetMapping("/books")
    public String userBooks() {
        return "user/books";
    }
}
