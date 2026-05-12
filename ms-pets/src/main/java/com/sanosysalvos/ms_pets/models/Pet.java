package com.sanosysalvos.ms_pets.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import java.time.LocalDate;

@Entity
@Table(name = "pets")
@Data
public class Pet {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id")
    private String userUid;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String especie;

    @Column(name = "color", nullable = true) // Cambiar a true
    private String color;

    private Integer edad;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(name = "fecha_perdida", nullable = false)
    private LocalDate fechaPerdida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetStatus estado;

    @Column(nullable = false, columnDefinition = "numeric")
    private Double latitud;

    @Column(nullable = false, columnDefinition = "numeric")
    private Double longitud;

    @Column(columnDefinition = "TEXT")
    private String foto;
    
    @Column(columnDefinition = "TEXT")
    private String direccionFormateada;
}