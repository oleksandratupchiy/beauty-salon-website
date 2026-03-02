package com.example.beautysalon.repository;

import com.example.beautysalon.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    // Тут нічого писати не треба, Spring сам усе зробить
}