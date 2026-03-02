package com.example.beautysalon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Ця анотація каже Спрінгу: "Шукай контролери в папці com.example.beautysalon і глибше"
@SpringBootApplication(scanBasePackages = "com.example.beautysalon")
public class BeautySalonApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeautySalonApplication.class, args);
    }

}