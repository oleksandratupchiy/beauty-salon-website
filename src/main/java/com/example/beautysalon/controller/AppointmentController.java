package com.example.beautysalon.controller;

import com.example.beautysalon.entity.Appointment;
import com.example.beautysalon.entity.Client;
import com.example.beautysalon.repository.AppointmentRepository;
import com.example.beautysalon.repository.ClientRepository;
import com.example.beautysalon.repository.MasterRepository;
import com.example.beautysalon.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private MasterRepository masterRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private ClientRepository clientRepository;

    @GetMapping("/appointment")
    public String showAppointmentForm(@RequestParam(required = false) Long serviceId, Model model) {
        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("masters", masterRepository.findAll());
        model.addAttribute("selectedServiceId", serviceId);
        return "appointment_page";
    }

    @PostMapping("/appointment/save")
    public String saveAppointment(@RequestParam Long serviceId,
                                  @RequestParam Long masterId,
                                  @RequestParam String date,
                                  @RequestParam String time,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {

        if (principal == null) return "redirect:/login";

        boolean isBusy = appointmentRepository.existsByMasterIdAndDateAndTime(masterId, date, time);

        if (isBusy) {
            redirectAttributes.addFlashAttribute("errorMessage", "Вибачте, цей час у майстра вже зайнятий. Оберіть інший!");
            return "redirect:/appointment";
        }

        // Отримуємо логін (у нашому випадку це Name, бо так в UserDetailsService)
        String currentLogin = principal.getName();
        Client client = clientRepository.findByName(currentLogin);
        if (client == null) {
            client = clientRepository.findByEmail(currentLogin);
        }

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setMaster(masterRepository.findById(masterId).orElse(null));
        appointment.setService(serviceRepository.findById(serviceId).orElse(null));
        appointment.setDate(date);
        appointment.setTime(time);

        appointmentRepository.save(appointment);

        return "redirect:/profile?success";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String currentLogin = principal.getName();

        // Шукаємо клієнта за іменем, яке прийшло з SecurityContext
        Client client = clientRepository.findByName(currentLogin);

        // Резервний пошук за емейлом
        if (client == null) {
            client = clientRepository.findByEmail(currentLogin);
        }

        // Якщо клієнта не знайдено, перенаправляємо на логін замість помилки
        if (client == null) {
            return "redirect:/login?error=notfound";
        }

        List<Appointment> appointments = appointmentRepository.findByClientId(client.getId());

        model.addAttribute("client", client);
        model.addAttribute("appointments", appointments);
        return "profile";
    }

    @PostMapping("/appointment/cancel")
    public String cancelAppointment(@RequestParam Long appointmentId, Principal principal) {
        if (principal == null) return "redirect:/login";

        appointmentRepository.deleteById(appointmentId);
        return "redirect:/profile?cancelled";
    }
}