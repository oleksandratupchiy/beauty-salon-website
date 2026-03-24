package com.example.beautysalon.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointment") // Чітко в однині, як у твоїй базі
@Data
@NoArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Поля для дати та часу
    private String date;
    private String time;

    // Зв'язок з Майстром (назва колонки як на скріні з pgAdmin)
    @ManyToOne
    @JoinColumn(name = "masterid")
    private Master master;

    // Зв'язок з Послугою
    @ManyToOne
    @JoinColumn(name = "serviceid")
    private Service service;

    // Зв'язок з Клієнтом
    @ManyToOne
    @JoinColumn(name = "clientid")
    private Client client;

    /**
     * Безпечний метод перевірки, чи запис уже в минулому.
     * Якщо дані пошкоджені або порожні, поверне false, а не WhiteLabel.
     */
    public boolean isPast() {
        if (this.date == null || this.time == null || this.date.isEmpty() || this.time.isEmpty()) {
            return false;
        }
        try {
            // Парсимо дату та час
            LocalDate d = LocalDate.parse(this.date);
            LocalTime t = LocalTime.parse(this.time);
            LocalDateTime start = LocalDateTime.of(d, t);

            // Беремо тривалість послуги або 60 хв за замовчуванням
            int duration = (this.service != null && this.service.getDuration() != null)
                    ? this.service.getDuration() : 60;

            // Перевіряємо, чи візит вже закінчився
            return start.plusMinutes(duration).isBefore(LocalDateTime.now());
        } catch (Exception e) {
            // Якщо формат дати в базі неправильний, просто ігноруємо помилку, щоб сторінка завантажилась
            return false;
        }
    }
}