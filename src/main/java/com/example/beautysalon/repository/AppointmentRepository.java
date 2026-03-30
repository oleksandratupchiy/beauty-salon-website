package com.example.beautysalon.repository;

import com.example.beautysalon.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    boolean existsByMasterIdAndDateAndTime(Long masterId, String date, String time);
    List<Appointment> findByClientId(Long clientId);
    List<Appointment> findAllByMasterIdAndDate(Long masterId, String date);
    // Додай це у файл AppointmentRepository.java
    boolean existsByClientIdAndMasterIdAndServiceIdAndDateAndTime(Long clientId, Long masterId, Long serviceId, String date, String time);
    // НОВИЙ МЕТОД ДЛЯ ПЕРЕВІРКИ ДУБЛІВ КЛІЄНТА
    boolean existsByClientIdAndDateAndTime(Long clientId, String date, String time);
}