package com.sanosysalvos.ms_users.dtos;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String firebaseUid; // Solo necesario en la creación
    private String nombre;
    private String rut;
    private String email;
    private String celular;
    private String direccionResidencia;
}