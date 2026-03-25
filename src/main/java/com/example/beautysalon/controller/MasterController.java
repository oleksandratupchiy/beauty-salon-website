package com.example.beautysalon.controller;

import com.example.beautysalon.entity.Client;
import com.example.beautysalon.entity.Master;
import com.example.beautysalon.repository.ClientRepository; // Додано імпорт
import com.example.beautysalon.repository.MasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class MasterController {

    @Autowired
    private MasterRepository masterRepository;

    // Додано підключення репозиторію клієнтів
    @Autowired
    private ClientRepository clientRepository;

    @GetMapping("/")
    public String showIndexPage(Model model, Principal principal) {
        // Якщо користувач авторизований
        if (principal != null) {
            // Шукаємо його в базі за email (логіном)
            Client client = clientRepository.findByEmail(principal.getName()).orElse(null);

            if (client != null) {
                // Передаємо реальне ім'я в HTML
                model.addAttribute("clientName", client.getName());
            }
        }
        return "index";
    }

    @GetMapping("/masters")
    public String showCategories(@RequestParam(required=false) String specialization,
                                 Model model) {
        List<Master> masters;
        if(specialization == null || specialization.isEmpty() || specialization.equals("all")){
            masters = masterRepository.findAll();
        } else {
            masters = masterRepository.findBySpecialization(specialization);
        }

        model.addAttribute("masters", masters);
        model.addAttribute("activeSpecialization", specialization);

        return "masters_page";
    }
}