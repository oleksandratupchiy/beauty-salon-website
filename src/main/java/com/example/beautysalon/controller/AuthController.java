package com.example.beautysalon.controller;

import com.example.beautysalon.entity.Client;
import com.example.beautysalon.repository.ClientRepository;
import com.example.beautysalon.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class AuthController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // 1. СТОРИНКА ВХОДУ
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // 2. СТОРИНКА РЕЄСТРАЦІЇ
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    // 3. ОБРОБКА РЕЄСТРАЦІЇ КЛІЄНТА
    @PostMapping("/register")
    public String registerClient(@RequestParam String name,
                                 @RequestParam String email,
                                 @RequestParam String password) {

        System.out.println("Реєстрація почалася для: " + email);

        // ДОДАНО: Перевірка довжини пароля
        if (password.length() < 8) {
            return "redirect:/register?short_password";
        }

        if (clientRepository.findByEmail(email).isPresent()) {
            return "redirect:/register?email_exists";
        }

        Client client = new Client();
        client.setName(name);
        client.setEmail(email);
        client.setRole("ROLE_USER");
        client.setPassword(passwordEncoder.encode(password));

        client.setEnabled(false);
        String randomCode = UUID.randomUUID().toString();
        client.setVerificationCode(randomCode);

        clientRepository.save(client);

        String verifyLink = "http://localhost:8080/verify?code=" + randomCode;
        String mailText = "Привіт, " + name + "!\n\n" +
                "Щоб підтвердити реєстрацію в DUNYA BEAUTY, перейди за цим посиланням:\n" +
                verifyLink;

        emailService.sendEmail(email, "Підтвердження реєстрації", mailText);

        return "redirect:/login?check_email";
    }

    // 4. ОБРОБКА КЛІКУ ПО ПОСИЛАННЮ З ЛИСТА
    @GetMapping("/verify")
    public String verifyUser(@RequestParam("code") String code) {
        Client client = clientRepository.findByVerificationCode(code).orElse(null);

        if (client == null) {
            return "redirect:/login?invalid_code";
        }

        client.setEnabled(true);
        client.setVerificationCode(null);
        clientRepository.save(client);

        return "redirect:/login?verified";
    }

    // 5. Показати сторінку "Забули пароль?"
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password";
    }

    // 6. Обробка форми "Забули пароль"
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email) {
        Client client = clientRepository.findByEmail(email).orElse(null);

        if (client == null) {
            return "redirect:/forgot-password?error";
        }

        String token = UUID.randomUUID().toString();
        client.setResetPasswordToken(token);
        clientRepository.save(client);

        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        String mailText = "Ви зробили запит на відновлення пароля.\n\n" +
                "Перейдіть за цим посиланням, щоб встановити новий пароль:\n" + resetLink + "\n\n" +
                "Якщо це були не ви, просто проігноруйте цей лист.";

        emailService.sendEmail(email, "Відновлення пароля | DUNYA BEAUTY", mailText);

        return "redirect:/login?reset_email_sent";
    }

    // 7. Показати сторінку введення нового пароля
    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
        Client client = clientRepository.findByResetPasswordToken(token).orElse(null);

        if (client == null) {
            return "redirect:/login?invalid_code";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    // 8. Збереження нового пароля
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String newPassword) {

        // ДОДАНО: Перевірка довжини нового пароля
        if (newPassword.length() < 8) {
            return "redirect:/reset-password?token=" + token + "&short_password";
        }

        Client client = clientRepository.findByResetPasswordToken(token).orElse(null);

        if (client == null) {
            return "redirect:/login?invalid_code";
        }

        client.setPassword(passwordEncoder.encode(newPassword));
        client.setResetPasswordToken(null);
        clientRepository.save(client);

        return "redirect:/login?password_reset";
    }
}