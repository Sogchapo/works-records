package com.works.worksrecords.controller;

import com.works.worksrecords.model.User;
import com.works.worksrecords.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/admin/users")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @GetMapping("/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/create-user";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute User user, 
                             @RequestParam(value = "isSuperadmin", defaultValue = "false") boolean isSuperadmin,
                             Model model) {
        if (userRepository.existsByUsername(user.getUsername())) {
            model.addAttribute("error", "Username already exists!");
            return "admin/create-user";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Use mutable HashSet for Hibernate JPA
        if (isSuperadmin) {
            user.setRoles(new HashSet<>(Set.of(User.Role.ROLE_USER, User.Role.ROLE_SUPERADMIN)));
        } else {
            user.setRoles(new HashSet<>(Set.of(User.Role.ROLE_USER)));
        }

        userRepository.save(user);
        return "redirect:/admin/users?success";
    }

    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + id));
        model.addAttribute("user", user);
        model.addAttribute("isSuperadmin", user.getRoles().contains(User.Role.ROLE_SUPERADMIN));
        return "admin/edit-user";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @ModelAttribute User userDetails,
                             @RequestParam(value = "isSuperadmin", defaultValue = "false") boolean isSuperadmin,
                             @RequestParam(value = "newPassword", required = false) String newPassword,
                             Model model) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + id));

        // Check for username conflict if changed
        if (!existingUser.getUsername().equals(userDetails.getUsername()) &&
                userRepository.existsByUsername(userDetails.getUsername())) {
            model.addAttribute("error", "Username already exists!");
            model.addAttribute("user", existingUser);
            model.addAttribute("isSuperadmin", isSuperadmin);
            return "admin/edit-user";
        }

        existingUser.setFullName(userDetails.getFullName());
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setActive(userDetails.isActive());

        // Update password only if a new non-empty password was provided
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }

        // Use mutable HashSet to allow Hibernate collection manipulation
        if (isSuperadmin) {
            existingUser.setRoles(new HashSet<>(Set.of(User.Role.ROLE_USER, User.Role.ROLE_SUPERADMIN)));
        } else {
            existingUser.setRoles(new HashSet<>(Set.of(User.Role.ROLE_USER)));
        }

        userRepository.save(existingUser);
        return "redirect:/admin/users?updated";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users?deleted";
    }
}