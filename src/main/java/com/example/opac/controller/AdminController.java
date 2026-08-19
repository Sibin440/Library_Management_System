package com.example.opac.controller;

import com.example.opac.service.UserService;
import com.example.opac.repository.RoleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import com.example.opac.model.Role;

@Controller
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public AdminController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }
    
    @GetMapping("/admin/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.listAllUsers());
        return "admin/users";
    }
    
    @PostMapping("/admin/users/{id}/roles")
    public String updateUserRoles(@PathVariable Long id,
                                  @RequestParam(required = false) List<String> roles) {
        Set<Role> newRoles = new HashSet<>();
        if (roles != null) {
            for (String r : roles) {
                roleRepository.findByName(r).ifPresent(newRoles::add);
            }
        }
        userService.updateUserRoles(id, newRoles);
        return "redirect:/admin/users";
    }
}
