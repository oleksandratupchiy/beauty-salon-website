package com.example.beautysalon.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "services")
@Data
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
}