package com.example.beautysalon.repository;

import com.example.beautysalon.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findByEmail(String email);
    Client findByName(String name); // Додай цей рядок обов'язково
}