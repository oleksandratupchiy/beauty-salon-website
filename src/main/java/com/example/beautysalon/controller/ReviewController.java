package com.example.beautysalon.controller;

import com.example.beautysalon.entity.*;
import com.example.beautysalon.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ReviewController {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private MasterRepository masterRepository;
    @Autowired private AppointmentRepository appointmentRepository;

    // 1. ПОКАЗАТИ ВСІ ВІДГУКИ
    @GetMapping("/reviews")
    public String showReviews(Model model) {
        model.addAttribute("reviews", reviewRepository.findAllByOrderByIdDesc());
        model.addAttribute("masters", masterRepository.findAll());
        return "reviews_page";
    }

    // 2. ФОРМА ДЛЯ ПІДТВЕРДЖЕНОГО ВІДГУКУ (З ПРОФІЛЮ)
    @GetMapping("/reviews/new")
    public String showReviewForm(@RequestParam Long appointmentId, Model model) {
        Appointment app = appointmentRepository.findById(appointmentId).orElse(null);
        if (app == null) return "redirect:/profile";

        model.addAttribute("app", app);
        return "review_form";
    }

    // 3. ЗБЕРЕЖЕННЯ ВІДГУКУ (КУЛЕНЕПРОБИВНЕ)
    @PostMapping("/reviews/add")
    public String addReview(@RequestParam Double rating,
                            @RequestParam String text,
                            @RequestParam String clientName,
                            @RequestParam String masterName,
                            @RequestParam String serviceType,
                            @RequestParam(required = false) Long appointmentId) {
        try {
            Review review = new Review();
            review.setRating(rating);
            review.setMasterName(masterName);
            review.setServiceType(serviceType);
            review.setCreatedAt(LocalDateTime.now());
            review.setText(clientName + ": " + text);

            // ПЕРЕВІРКА: Чи існує реально такий візит у базі?
            if (appointmentId != null) {
                // Використовуємо .existsById, щоб не створювати FK на пусте місце
                if (appointmentRepository.existsById(appointmentId)) {
                    appointmentRepository.findById(appointmentId).ifPresent(review::setAppointment);
                } else {
                    // Якщо візиту з таким ID вже немає - просто зберігаємо як звичайний відгук
                    System.out.println("DEBUG: Візит з ID " + appointmentId + " не знайдено. Зберігаємо без прив'язки.");
                }
            }

            reviewRepository.save(review);
            updateMasterRating(masterName);

            return "redirect:/reviews";
        } catch (Exception e) {
            // Якщо база все одно лається на Foreign Key - зберігаємо ВЗАГАЛІ без appointmentId
            System.err.println("ПОМИЛКА FK: " + e.getMessage());
            return "redirect:/reviews?error_fk";
        }
    }

    // Допоміжний метод для розрахунку середнього рейтингу
    private void updateMasterRating(String masterName) {
        Master master = masterRepository.findAll().stream()
                .filter(m -> m.getName().equals(masterName))
                .findFirst().orElse(null);

        if (master != null) {
            List<Review> masterReviews = reviewRepository.findAll().stream()
                    .filter(r -> r.getMasterName().equals(masterName))
                    .toList();

            double average = masterReviews.stream()
                    .mapToDouble(Review::getRating)
                    .average()
                    .orElse(5.0);

            master.setRating(Math.round(average * 10.0) / 10.0);
            masterRepository.save(master);
        }
    }
}