package com.example.opac.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdminController {

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }
    
    @GetMapping("/admin/users")
    public String listUsers(org.springframework.ui.Model model, com.example.opac.service.UserService userService) {
        model.addAttribute("users", userService.listAllUsers());
        return "admin/users";
    }
    
    @PostMapping("/admin/users/{id}/roles")
    public String updateUserRoles(@org.springframework.web.bind.annotation.PathVariable Long id,
                                  @org.springframework.web.bind.annotation.RequestParam(required = false) java.util.List<String> roles,
                                  com.example.opac.service.UserService userService,
                                  com.example.opac.repository.RoleRepository roleRepository) {
        java.util.Set<com.example.opac.model.Role> newRoles = new java.util.HashSet<>();
        if (roles != null) {
            for (String r : roles) {
                roleRepository.findByName(r).ifPresent(newRoles::add);
            }
        }
        userService.updateUserRoles(id, newRoles);
        return "redirect:/admin/users";
    }
}
