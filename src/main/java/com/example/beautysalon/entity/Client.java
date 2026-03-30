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

    // Поля для безпеки
    private String password;
    private String role = "ROLE_USER";

    // --- НОВІ ПОЛЯ ДЛЯ ПОШТИ ТА ПАРОЛЯ ---

    @Column(name = "enabled")
    private Boolean enabled = false; // За замовчуванням акаунт неактивний

    @Column(name = "verification_code", length = 64)
    private String verificationCode; // Код для підтвердження пошти

    @Column(name = "reset_password_token", length = 64)
    private String resetPasswordToken; // Токен для скидання пароля

    // -------------------------------------

    @OneToMany(mappedBy = "client")
    private List<Appointment> appointments;
}