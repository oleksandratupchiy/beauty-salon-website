package com.example.beautysalon.repository;

import com.example.beautysalon.entity.Master;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MasterRepository extends JpaRepository<Master, Long> {
    List<Master> findBySpecialization(String specialization);
    Optional<Master> findByName(String name);
}