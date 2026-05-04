package com.sanosysalvos.ms_pets.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import java.time.LocalDate; // Para arreglar el error de fechaPerdida

@Entity
@Table(name = "pets")
@Data
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId; 

    private String nombre;
    private String especie; // <--- Mantenla, es necesaria
    private String color;
    private String edad;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_perdida")
    private LocalDate fechaPerdida; 

    @Enumerated(EnumType.STRING)
    private PetStatus estado; 

    private Double latitud;
    private Double longitud;

    @Column(columnDefinition = "TEXT")
    private String foto;
}