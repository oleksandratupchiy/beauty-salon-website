package com.example.beautysalon.controller;

import com.example.beautysalon.entity.Master;
import com.example.beautysalon.repository.MasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Цей імпорт дуже важливий для Model!
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MasterController {

    @Autowired
    private MasterRepository masterRepository;
    @GetMapping("/")
    public String showHomePage() {
        return "index";
    }

    @GetMapping("/masters")
    public String showCategories(@RequestParam(required=false)
            String specialization,
            Model model) {

        List<Master> masters;

        if(specialization == null || specialization.isEmpty()
                || specialization.equals("all")){
            masters = masterRepository.findAll();
        }
        else masters = masterRepository.findBySpecialization(specialization);

        model.addAttribute("masters", masters);
        model.addAttribute("activeSpecialization", specialization);

        return "masters_page"; 
    }
}