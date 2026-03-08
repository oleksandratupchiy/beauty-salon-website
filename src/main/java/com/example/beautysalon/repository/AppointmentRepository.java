package com.example.beautysalon.repository;

import com.example.beautysalon.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Перевірка, чи вільний майстер (тепер по двох полях)
    boolean existsByMasterIdAndDateAndTime(Long masterId, String date, String time);

    // Знайти всі записи конкретного клієнта за його ID
    List<Appointment> findByClientId(Long clientId);
}