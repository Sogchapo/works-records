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
    public String createUser(@RequestParam("fullName") String fullName,
                             @RequestParam("username") String username,
                             @RequestParam("password") String password,
                             @RequestParam(value = "role", defaultValue = "EMPLOYEE") String role,
                             Model model) {

        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Username already exists!");
            model.addAttribute("user", new User());
            return "admin/create-user";
        }

        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);

        Set<User.Role> roles = new HashSet<>();
        roles.add(User.Role.ROLE_USER); // Base application access

        switch (role.toUpperCase()) {
            case "SUPERADMIN":
                roles.add(User.Role.ROLE_SUPERADMIN);
                break;
            case "HOD":
                roles.add(User.Role.ROLE_HOD);
                break;
            case "EMPLOYEE":
            default:
                roles.add(User.Role.ROLE_EMPLOYEE);
                break;
        }

        user.setRoles(roles);

        userRepository.save(user);
        return "redirect:/admin/users?success";
    }

    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + id));
        model.addAttribute("user", user);
        
        // Determine primary role for selection dropdown
        String currentRole = "EMPLOYEE";
        if (user.getRoles().contains(User.Role.ROLE_SUPERADMIN)) {
            currentRole = "SUPERADMIN";
        } else if (user.getRoles().contains(User.Role.ROLE_HOD)) {
            currentRole = "HOD";
        }
        
        model.addAttribute("currentRole", currentRole);
        return "admin/edit-user";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable("id") Long id,
                             @RequestParam("fullName") String fullName,
                             @RequestParam("username") String username,
                             @RequestParam(value = "role", defaultValue = "EMPLOYEE") String role,
                             @RequestParam(value = "newPassword", required = false) String newPassword,
                             @RequestParam(value = "active", defaultValue = "true") boolean active,
                             Model model) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + id));

        // Check for username collision if changed
        if (!existingUser.getUsername().equals(username) &&
                userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Username already exists!");
            model.addAttribute("user", existingUser);
            model.addAttribute("currentRole", role);
            return "admin/edit-user";
        }

        existingUser.setFullName(fullName);
        existingUser.setUsername(username);
        existingUser.setActive(active);

        // Update password if a new one was provided
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }

        // Re-map role assignments
        Set<User.Role> roles = new HashSet<>();
        roles.add(User.Role.ROLE_USER);

        switch (role.toUpperCase()) {
            case "SUPERADMIN":
                roles.add(User.Role.ROLE_SUPERADMIN);
                break;
            case "HOD":
                roles.add(User.Role.ROLE_HOD);
                break;
            case "EMPLOYEE":
            default:
                roles.add(User.Role.ROLE_EMPLOYEE);
                break;
        }

        existingUser.setRoles(roles);

        userRepository.save(existingUser);
        return "redirect:/admin/users?updated";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users?deleted";
    }
}