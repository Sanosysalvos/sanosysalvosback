package com.sanosysalvos.ms_users.models;

import jakarta.persistence.*; // Esto soluciona @Entity, @Table, @Id, @Column
import lombok.Data;            // Esto soluciona @Data
import java.util.UUID;          // Esto soluciona el tipo UUID
import java.time.LocalDateTime; // Esto soluciona LocalDateTime
import com.fasterxml.jackson.annotation.JsonProperty; // Importante

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // Esta anotación le dice a Java: "Si recibes 'firebase_uid' en el JSON, guárdalo aquí"
    @JsonProperty("firebase_uid") 
    @Column(name = "firebase_uid", unique = true, nullable = false)
    private String firebaseUid;

    private String nombre;

    @Column(name = "rut")
    private String rut;

    private String email;

    private String celular;

    @JsonProperty("direccionResidencia") // <--- Así debe coincidir con React
    @Column(name = "direccion_residencia") // <--- Así coincide con la DB
    private String direccionResidencia;

    @JsonProperty("is_admin")
    @Column(name = "is_admin")
    private Boolean isAdmin = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}