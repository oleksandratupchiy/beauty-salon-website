package com.example.beautysalon.controller;

import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.beautysalon.entity.Appointment;
import com.example.beautysalon.entity.Review;
import com.example.beautysalon.entity.Service;
import com.example.beautysalon.entity.Master;
import com.example.beautysalon.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private ServiceRepository serviceRepository;
    @Autowired private MasterRepository masterRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private ReviewRepository reviewRepository;

    // --- ДАШБОРД (АНАЛІТИКА) ---
    @GetMapping("/dashboard")
    public String showDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("servicesCount", serviceRepository.count());
        model.addAttribute("mastersCount", masterRepository.count());
        model.addAttribute("appointmentsCount", appointmentRepository.count());

        String adminName = (userDetails != null) ? userDetails.getUsername() : "Адмін";
        model.addAttribute("welcomeMessage", "Вітаємо, " + adminName + "!");

        ObjectMapper mapper = new ObjectMapper();

        try {
            List<Appointment> allApps = appointmentRepository.findAll();
            Map<String, String> categoryNames = getCategoryMap();

            Map<String, Long> stats = allApps.stream()
                    .filter(app -> app.getService() != null)
                    .collect(Collectors.groupingBy(app -> {
                        String cat = app.getService().getCategory();
                        return categoryNames.getOrDefault(cat, (cat == null || cat.isEmpty()) ? "Інше" : cat);
                    }, Collectors.counting()));

            List<List<Object>> chartDataList = new ArrayList<>();
            chartDataList.add(Arrays.asList("Категорія", "Кількість"));
            stats.forEach((category, count) -> chartDataList.add(Arrays.asList(category, count)));

            if (chartDataList.size() == 1) chartDataList.add(Arrays.asList("Немає записів", 1));
            model.addAttribute("chartData", mapper.writeValueAsString(chartDataList));

            List<Review> allReviews = reviewRepository.findAll();
            Map<String, Double> masterStats = allReviews.stream()
                    .filter(r -> r.getMasterName() != null)
                    .collect(Collectors.groupingBy(Review::getMasterName, Collectors.averagingDouble(Review::getRating)));

            List<List<Object>> masterDataList = new ArrayList<>();
            masterDataList.add(Arrays.asList("Майстер", "Рейтинг", Collections.singletonMap("role", "style")));

            String[] colors = {"#fed8ab", "#8a7af7", "#4aedfe", "#5af7ae", "#e472d2", "#cdff30", "#000181", "#7c0116"};
            List<Map.Entry<String, Double>> sorted = masterStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .collect(Collectors.toList());

            for (int i = 0; i < sorted.size(); i++) {
                masterDataList.add(Arrays.asList(sorted.get(i).getKey(), sorted.get(i).getValue(), colors[i % colors.length]));
            }

            if (masterDataList.size() == 1) masterDataList.add(Arrays.asList("Немає відгуків", 0.0, "#d3d3d3"));
            model.addAttribute("masterData", mapper.writeValueAsString(masterDataList));

        } catch (Exception e) {
            model.addAttribute("chartData", "[[\"Категорія\", \"Кількість\"], [\"Помилка\", 1]]");
            model.addAttribute("masterData", "[[\"Майстер\", \"Рейтинг\", {\"role\": \"style\"}], [\"Помилка\", 0, \"#ff0000\"]]");
        }
        return "admin/dashboard";
    }

    // --- КЕРУВАННЯ ПОСЛУГАМИ ---
    @GetMapping("/services")
    public String viewServices(Model model) {
        model.addAttribute("services", serviceRepository.findAll());
        return "admin/services";
    }

    @PostMapping("/services/save")
    public String saveService(@ModelAttribute Service service, RedirectAttributes redirectAttrs) {
        serviceRepository.save(service);
        redirectAttrs.addFlashAttribute("successMsg", "Послугу успішно збережено!");
        return "redirect:/admin/services";
    }

    @GetMapping("/services/delete/{id}")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            serviceRepository.deleteById(id);
            redirectAttrs.addFlashAttribute("successMsg", "Послугу видалено!");
        } catch (DataIntegrityViolationException e) {
            redirectAttrs.addFlashAttribute("errorMsg", "Неможливо видалити послугу: до неї прив'язані записи клієнтів!");
        }
        return "redirect:/admin/services";
    }

    // --- КЕРУВАННЯ МАЙСТРАМИ ---
    @GetMapping("/masters")
    public String viewMasters(Model model) {
        model.addAttribute("masters", masterRepository.findAll());
        return "admin/masters";
    }

    @PostMapping("/masters/save")
    public String saveMaster(@ModelAttribute Master master, RedirectAttributes redirectAttrs) {
        masterRepository.save(master);
        redirectAttrs.addFlashAttribute("successMsg", "Дані майстра оновлено!");
        return "redirect:/admin/masters";
    }

    @GetMapping("/masters/delete/{id}")
    public String deleteMaster(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            masterRepository.deleteById(id);
            redirectAttrs.addFlashAttribute("successMsg", "Майстра успішно видалено!");
        } catch (DataIntegrityViolationException e) {
            redirectAttrs.addFlashAttribute("errorMsg", "Неможливо видалити майстра: у нього є заплановані візити!");
        }
        return "redirect:/admin/masters";
    }

    // --- КЕРУВАННЯ ЗАПИСАМИ (З ДЕТЕКТОРОМ КОНФЛІКТІВ) ---
    @GetMapping("/appointments")
    public String viewAppointments(Model model) {
        List<Appointment> appointments = appointmentRepository.findAll();

        // 1. Знаходимо ID записів, які перетинаються (однаковий майстер + дата + час)
        Set<Long> conflictIds = appointments.stream()
                .filter(app -> app.getMaster() != null && app.getDate() != null && app.getTime() != null)
                .collect(Collectors.groupingBy(
                        app -> app.getMaster().getId() + "|" + app.getDate() + "|" + app.getTime(),
                        Collectors.toList()
                ))
                .values().stream()
                .filter(list -> list.size() > 1) // Групи, де більше одного запису на один час
                .flatMap(list -> list.stream().map(Appointment::getId))
                .collect(Collectors.toSet());

        model.addAttribute("appointments", appointments);
        model.addAttribute("conflictIds", conflictIds); // Відправляємо на фронт набір ID для підсвічування
        return "admin/appointments";
    }

    @GetMapping("/appointments/delete/{id}")
    public String deleteAppointment(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        appointmentRepository.deleteById(id);
        redirectAttrs.addFlashAttribute("successMsg", "Запис успішно видалено!");
        return "redirect:/admin/appointments";
    }

    @PostMapping("/appointments/reschedule")
    public String rescheduleAppointmentAdmin(@RequestParam("id") Long id,
                                             @RequestParam("date") String date,
                                             @RequestParam("time") String time,
                                             RedirectAttributes redirectAttrs) {
        Appointment appointment = appointmentRepository.findById(id).orElse(null);
        if (appointment != null) {
            // Перевіряємо, чи вільний майстер, щоб попередити адміна
            boolean isAlreadyBusy = appointmentRepository.existsByMasterIdAndDateAndTime(
                    appointment.getMaster().getId(), date, time);

            appointment.setDate(date);
            appointment.setTime(time);
            appointmentRepository.save(appointment);

            if (isAlreadyBusy) {
                redirectAttrs.addFlashAttribute("warningMsg", "Візит перенесено, але УВАГА: у майстра вже є інший запис на цей час!");
            } else {
                redirectAttrs.addFlashAttribute("successMsg", "Час візиту успішно змінено!");
            }
        }
        return "redirect:/admin/appointments";
    }

    private Map<String, String> getCategoryMap() {
        Map<String, String> names = new HashMap<>();
        names.put("nails", "Нігтьовий сервіс");
        names.put("female_cut", "Жіночі стрижки");
        names.put("male_cut", "Чоловічі стрижки");
        names.put("makeup", "Візаж");
        names.put("permanent", "Перманент");
        names.put("cosmetician", "Косметологія");
        names.put("lash_brows", "Брови & Вії");
        names.put("colorist", "Колористика");
        return names;
    }
}