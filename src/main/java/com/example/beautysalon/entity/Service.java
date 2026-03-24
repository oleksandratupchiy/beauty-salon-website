package com.example.beautysalon.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor // Генерує порожній конструктор
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private Double price;
    private Integer duration;
    private String category;

    @ManyToMany(mappedBy = "services")
    private List<Master> masters;

    // Конструктор для імпорту з Excel
    public Service(String title, Double price, Integer duration, String category) {
        this.title = title;
        this.price = price;
        this.duration = duration;
        this.category = category;
    }
}