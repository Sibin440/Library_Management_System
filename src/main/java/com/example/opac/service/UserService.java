package com.example.opac.service;

import com.example.opac.model.Role;
import com.example.opac.model.User;
import com.example.opac.repository.RoleRepository;
import com.example.opac.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public java.util.List<User> listAllUsers() {
        return userRepository.findAll();
    }

    public void updateUserRoles(Long userId, java.util.Set<Role> roles) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setRoles(roles);
            userRepository.save(u);
        });
    }

    public User registerNewUser(String username, String email, String rawPassword) {
        User user = new User(username, email, passwordEncoder.encode(rawPassword));
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public void createAdminIfNotExists(String username, String email, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()) return;
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));
        User admin = new User(username, email, passwordEncoder.encode(rawPassword));
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(userRole);
        admin.setRoles(roles);
        admin.setEnabled(true);
        userRepository.save(admin);
    }
}
