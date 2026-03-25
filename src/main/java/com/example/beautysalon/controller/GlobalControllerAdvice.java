package com.example.beautysalon.controller;

import com.example.beautysalon.entity.Client;
import com.example.beautysalon.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private ClientRepository clientRepository;

    // Цей метод автоматично виконується ПЕРЕД завантаженням БУДЬ-ЯКОЇ сторінки
    @ModelAttribute
    public void addClientNameToModel(Model model, Principal principal) {
        if (principal != null) {
            Client client = clientRepository.findByEmail(principal.getName()).orElse(null);
            if (client != null) {
                model.addAttribute("clientName", client.getName());
                // Заодно передаємо весь об'єкт client, якщо він раптом десь знадобиться
                model.addAttribute("client", client);
            }
        }
    }
}