package com.example.beautysalon.repository;
import java.util.List;
import com.example.beautysalon.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByOrderByIdDesc();
}