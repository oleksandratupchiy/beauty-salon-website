package com.example.beautysalon.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "masters")
@Data
@NoArgsConstructor
public class Master {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String specialization;
    private Double rating;

    @ManyToMany
    @JoinTable(
            name = "master_service",
            joinColumns = @JoinColumn(name = "master_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private List<Service> services;

    // Конструктор для імпорту з Excel
    public Master(String name) {
        this.name = name;
        this.rating = 5.0; // Дефолтний рейтинг для нових майстрів
        this.specialization = "Універсал"; // Дефолтна спеціалізація
    }
}