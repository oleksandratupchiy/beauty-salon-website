package com.example.beautysalon.repository;

import com.example.beautysalon.entity.Master;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MasterRepository extends JpaRepository<Master, Long> {
    List<Master> findBySpecialization(String specialization);
}