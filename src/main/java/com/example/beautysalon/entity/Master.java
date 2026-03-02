package com.example.beautysalon.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "masters")
@Data
public class Master {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String specialization;
    private Double rating;

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }


    @ManyToMany
    @JoinTable(
            name = "master_service",
            joinColumns = @JoinColumn(name = "master_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private List<Service> services;

}