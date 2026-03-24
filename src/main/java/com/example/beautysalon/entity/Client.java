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

    @Column(unique = true, nullable = false)
    private String email;

    // Нові поля для безпеки
    private String password;
    private String role = "ROLE_USER";

    @OneToMany(mappedBy = "client")
    private List<Appointment> appointments;
}