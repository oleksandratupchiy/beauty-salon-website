package com.example.beautysalon.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "clients")
@Data
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String email;

    // Зв'язок з записами (один клієнт може мати багато записів)
    @OneToMany(mappedBy = "client")
    private List<Appointment> appointments;
}