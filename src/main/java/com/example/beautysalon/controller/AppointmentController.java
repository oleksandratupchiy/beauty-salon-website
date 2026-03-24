package com.example.beautysalon.controller;

import com.example.beautysalon.entity.*;
import com.example.beautysalon.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AppointmentController {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private MasterRepository masterRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private ClientRepository clientRepository;

    @GetMapping("/appointment")
    public String showAppointmentForm(@RequestParam(required = false) Long serviceId,
                                      @RequestParam(required = false) Long masterId, // <--- ДОДАНО: приймаємо ID майстра
                                      @RequestParam(required = false) Long rescheduleId,
                                      @RequestParam(required = false) String warning,
                                      @RequestParam(required = false) String tempDate,
                                      @RequestParam(required = false) String tempTime,
                                      Model model, Principal principal) {
        if (principal != null) {
            String login = principal.getName();
            Client client = clientRepository.findByName(login).orElse(null);
            if (client == null) client = clientRepository.findByEmail(login).orElse(null);
            if (client != null) {
                model.addAttribute("clientName", client.getName());
                model.addAttribute("clientPhone", client.getPhone());
            }
        }

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        model.addAttribute("minDate", tomorrow.toString());
        model.addAttribute("maxDate", tomorrow.plusDays(21).toString());

        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("masters", masterRepository.findAll());
        model.addAttribute("selectedServiceId", serviceId);
        model.addAttribute("selectedMasterId", masterId); // <--- ДОДАНО: передаємо ID у форму
        model.addAttribute("rescheduleId", rescheduleId);

        // Передаємо попередження, якщо клієнт намагається зробити дубль
        model.addAttribute("warning", warning);
        model.addAttribute("tempDate", tempDate);
        model.addAttribute("tempTime", tempTime);

        return "appointment_page";
    }

    @GetMapping("/appointment/available-slots")
    @ResponseBody
    public List<String> getAvailableSlots(@RequestParam Long masterId,
                                          @RequestParam Long serviceId,
                                          @RequestParam String date) {
        Service currentService = serviceRepository.findById(serviceId).orElse(null);
        if (currentService == null) return new ArrayList<>();

        int duration = (currentService.getDuration() != null) ? currentService.getDuration() : 60;
        List<Appointment> existingApps = appointmentRepository.findAllByMasterIdAndDate(masterId, date);

        List<String> slots = new ArrayList<>();
        LocalTime time = LocalTime.of(9, 0);
        LocalTime endDay = LocalTime.of(19, 0);

        while (time.plusMinutes(duration).isBefore(endDay.plusMinutes(1))) {
            LocalTime slotStart = time;
            LocalTime slotEnd = time.plusMinutes(duration);

            boolean overlaps = false;
            for (Appointment app : existingApps) {
                LocalTime exStart = LocalTime.parse(app.getTime());
                int exDur = (app.getService().getDuration() != null) ? app.getService().getDuration() : 60;
                LocalTime exEnd = exStart.plusMinutes(exDur);

                if (slotStart.isBefore(exEnd) && slotEnd.isAfter(exStart)) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) slots.add(slotStart.toString());
            time = time.plusMinutes(15);
        }
        return slots;
    }

    @PostMapping("/appointment/save")
    public String saveAppointment(@RequestParam Long serviceId,
                                  @RequestParam Long masterId,
                                  @RequestParam String date,
                                  @RequestParam String time,
                                  @RequestParam(required = false) Long rescheduleId,
                                  @RequestParam(required = false) Boolean forceSave,
                                  @RequestParam(required = false) String clientPhone,
                                  Principal principal) {
        if (principal == null) return "redirect:/login";

        Client client = clientRepository.findByName(principal.getName()).orElse(null);
        if (client == null) client = clientRepository.findByEmail(principal.getName()).orElse(null);
        if (client == null) return "redirect:/login";

        // Оновлюємо номер у профілі клієнта, якщо він ввів його (або змінив) при записі
        if (clientPhone != null && !clientPhone.isEmpty()) {
            client.setPhone(clientPhone);
            clientRepository.save(client);
        }

        // ПЕРЕВІРКА НА ДУБЛЮВАННЯ ЧАСУ
        if (rescheduleId == null && (forceSave == null || !forceSave)) {
            boolean hasDuplicate = appointmentRepository.existsByClientIdAndDateAndTime(client.getId(), date, time);
            if (hasDuplicate) {
                return "redirect:/appointment?serviceId=" + serviceId + "&warning=duplicate&tempDate=" + date + "&tempTime=" + time;
            }
        }

        Appointment app;
        if (rescheduleId != null) {
            app = appointmentRepository.findById(rescheduleId).orElse(new Appointment());
        } else {
            app = new Appointment();
        }

        app.setClient(client);
        app.setMaster(masterRepository.findById(masterId).orElse(null));
        app.setService(serviceRepository.findById(serviceId).orElse(null));
        app.setDate(date);
        app.setTime(time);

        appointmentRepository.save(app);

        return "redirect:/profile?success";
    }

    @PostMapping("/appointment/cancel")
    public String cancelAppointment(@RequestParam Long appointmentId) {
        appointmentRepository.deleteById(appointmentId);
        return "redirect:/profile?cancelled";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Client client = clientRepository.findByName(principal.getName()).orElse(null);
        if (client == null) client = clientRepository.findByEmail(principal.getName()).orElse(null);

        if (client != null) {
            model.addAttribute("client", client);
            model.addAttribute("appointments", appointmentRepository.findByClientId(client.getId()));
        }
        return "profile";
    }

    @PostMapping("/appointment/quick-reschedule")
    public String quickReschedule(@RequestParam Long appointmentId,
                                  @RequestParam String date,
                                  @RequestParam String time) {
        Appointment app = appointmentRepository.findById(appointmentId).orElse(null);
        if (app != null) {
            app.setDate(date);
            app.setTime(time);
            appointmentRepository.save(app);
        }
        return "redirect:/profile?success";
    }

    @PostMapping("/profile/edit")
    public String editProfile(@RequestParam String name,
                              @RequestParam String phone,
                              Principal principal) {
        if (principal == null) return "redirect:/login";

        Client client = clientRepository.findByName(principal.getName()).orElse(null);
        if (client == null) client = clientRepository.findByEmail(principal.getName()).orElse(null);

        if (client != null) {
            client.setName(name);
            client.setPhone(phone);
            clientRepository.save(client);
        }

        return "redirect:/profile?updated";
    }
}