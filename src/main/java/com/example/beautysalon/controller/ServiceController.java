package com.example.beautysalon.controller;

import com.example.beautysalon.entity.Service;
import com.example.beautysalon.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping("/services")
    public String showServices(Model model) {
        List<Service> allServices = serviceRepository.findAll();
        model.addAttribute("services", allServices);

        return "services_page";
    }
}