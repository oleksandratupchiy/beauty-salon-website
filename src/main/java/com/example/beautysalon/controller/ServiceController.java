package com.example.beautysalon.controller;

import com.example.beautysalon.entity.Service;
import com.example.beautysalon.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Цей імпорт дуже важливий для Model!
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List; // Для списку майстрів

@Controller // Тепер він знову шукатиме HTML
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping("/services")
    public String showServices(Model model) {
        List<Service> services = serviceRepository.findAll();
        model.addAttribute("services", services);
        return "services_page";
    }
}