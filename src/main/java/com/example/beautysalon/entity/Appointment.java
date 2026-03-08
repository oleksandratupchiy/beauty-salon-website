package com.example.beautysalon.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "appointment")
@Data
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String date; // Тепер контролер побачить setDate()
    private String time; // Тепер контролер побачить setTime()

    @ManyToOne
    @JoinColumn(name = "master_id")
    private Master master;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
}