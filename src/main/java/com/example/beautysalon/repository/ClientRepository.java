package com.example.beautysalon.repository;

import com.example.beautysalon.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);
    Optional<Client> findByName(String name);

    // --- НОВІ МЕТОДИ ДЛЯ ПОШТИ ТА ПАРОЛЯ ---
    Optional<Client> findByVerificationCode(String verificationCode);
    Optional<Client> findByResetPasswordToken(String token);
}