package com.example.beautysalon.controller;

import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.beautysalon.entity.Appointment;
import com.example.beautysalon.entity.Review;
import com.example.beautysalon.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private ServiceRepository serviceRepository;
    @Autowired private MasterRepository masterRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private ReviewRepository reviewRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("servicesCount", serviceRepository.count());
        model.addAttribute("mastersCount", masterRepository.count());
        model.addAttribute("appointmentsCount", appointmentRepository.count());

        String adminName = (userDetails != null) ? userDetails.getUsername() : "Адмін";
        model.addAttribute("welcomeMessage", "Вітаємо, " + adminName + "!");

        ObjectMapper mapper = new ObjectMapper();

        try {
            // --- 1. КРУГОВИЙ ГРАФІК (Послуги) ---
            List<Appointment> allApps = appointmentRepository.findAll();
            Map<String, Long> stats = allApps.stream()
                    .filter(app -> app.getService() != null && app.getService().getTitle() != null)
                    // БЕРЕМО НАЗВУ ПОСЛУГИ
                    .collect(Collectors.groupingBy(app -> app.getService().getTitle(), Collectors.counting()));

            List<List<Object>> chartDataList = new ArrayList<>();
            chartDataList.add(Arrays.asList("Послуга", "Кількість"));
            stats.forEach((title, count) -> chartDataList.add(Arrays.asList(title, count)));

            // Якщо записів немає, додаємо заглушку, щоб графік не зникав
            if (chartDataList.size() == 1) {
                chartDataList.add(Arrays.asList("Немає записів", 1));
            }
            model.addAttribute("chartData", mapper.writeValueAsString(chartDataList));

            // --- 2. СТОВПЧИКОВИЙ ГРАФІК (Майстри) ---
            List<Review> allReviews = reviewRepository.findAll();
            Map<String, Double> masterStats = allReviews.stream()
                    .filter(r -> r.getMasterName() != null)
                    .collect(Collectors.groupingBy(Review::getMasterName, Collectors.averagingDouble(Review::getRating)));

            List<List<Object>> masterDataList = new ArrayList<>();

            // Правильний формат стилю для Google Charts
            Map<String, String> styleCol = new HashMap<>();
            styleCol.put("role", "style");
            styleCol.put("type", "string");
            masterDataList.add(Arrays.asList("Майстер", "Рейтинг", styleCol));

            String[] colors = {"#4e73df", "#1cc88a", "#36b9cc", "#f6c23e", "#e74a3b"};
            List<Map.Entry<String, Double>> sorted = masterStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .collect(Collectors.toList());

            for (int i = 0; i < sorted.size(); i++) {
                masterDataList.add(Arrays.asList(sorted.get(i).getKey(), sorted.get(i).getValue(), colors[i % colors.length]));
            }

            // Якщо відгуків немає, додаємо заглушку
            if (masterDataList.size() == 1) {
                masterDataList.add(Arrays.asList("Немає відгуків", 0.0, "#d3d3d3"));
            }
            model.addAttribute("masterData", mapper.writeValueAsString(masterDataList));

        } catch (Exception e) {
            model.addAttribute("chartData", "[[\"Послуга\", \"Кількість\"], [\"Помилка\", 1]]");
            model.addAttribute("masterData", "[[\"Майстер\", \"Рейтинг\", {\"role\": \"style\", \"type\": \"string\"}], [\"Помилка\", 0, \"#ff0000\"]]");
        }

        return "admin/dashboard";
    }

    @GetMapping("/appointments")
    public String viewAppointments(Model model) {
        model.addAttribute("appointments", appointmentRepository.findAll());
        return "admin/appointments";
    }

    @GetMapping("/appointments/delete/{id}")
    public String deleteAppointment(@PathVariable Long id) {
        appointmentRepository.deleteById(id);
        return "redirect:/admin/appointments";
    }
}