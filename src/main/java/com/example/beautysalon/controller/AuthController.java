package com.example.beautysalon.controller;

import com.example.beautysalon.entity.Client;
import com.example.beautysalon.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. СТОРИНКА ВХОДУ
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // Повертає login.html з папки templates
    }

    // 2. СТОРИНКА РЕЄСТРАЦІЇ
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // Повертає register.html
    }

    // 3. ОБРОБКА РЕЄСТРАЦІЇ КЛІЄНТА
    @PostMapping("/register")
    public String registerClient(@RequestParam String name,
                                 @RequestParam String email,
                                 @RequestParam String password) {

        System.out.println("Реєстрація почалася для: " + email); // Дивись у консоль IntelliJ

        Client client = new Client();
        client.setName(name);
        client.setEmail(email);
        client.setRole("ROLE_USER");
        client.setPassword(passwordEncoder.encode(password));

        Client savedClient = clientRepository.save(client);
        System.out.println("Клієнта збережено з ID: " + savedClient.getId());

        return "redirect:/login?success";
    }
}