package com.example.beautysalon.controller;
import com.example.beautysalon.entity.Review;
import com.example.beautysalon.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.beautysalon.repository.MasterRepository;
import com.example.beautysalon.entity.Master;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MasterRepository masterRepository;

    @GetMapping("/reviews")
    public String showReviews(Model model) {
        List<Review> reviews = reviewRepository.findAllByOrderByIdDesc();
        List<Master> masters = masterRepository.findAll();

        model.addAttribute("reviews", reviews);
        model.addAttribute("masters", masters);
        return "reviews_page";
    }

    @PostMapping("/reviews/add")
    public String addReview(@RequestParam Double rating,
                            @RequestParam String text,
                            @RequestParam String clientName,
                            @RequestParam String masterName,
                            @RequestParam String serviceType) {
        Review review = new Review();

        review.setRating(rating);
        review.setMasterName(masterName);
        review.setServiceType(serviceType);
        review.setCreatedAt(LocalDateTime.now());

        review.setText(clientName + "\n" + text);
        reviewRepository.save(review);

//далі динамічне оновлення рейтингу майстра на основі відгуків
        Master master = masterRepository.findAll().stream()
                .filter(m -> m.getName().equals(masterName))
                .findFirst()
                .orElse(null);
        if(master!=null){
            List<Review> masterReviews=reviewRepository.findAll().stream()
                    .filter(r -> r.getMasterName().equals(masterName))
                    .toList();

            double average = masterReviews.stream()
                    .mapToDouble(Review::getRating)
                    .average()
                    .orElse(5.0);

            double roundedRating = Math.round(average * 10.0) / 10.0;

            master.setRating(roundedRating);
            masterRepository.save(master);
        }

        return "redirect:/reviews";
    }
}