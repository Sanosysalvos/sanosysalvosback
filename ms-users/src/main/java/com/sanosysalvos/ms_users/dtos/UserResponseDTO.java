package com.sanosysalvos.ms_users.dtos;

import lombok.Data;

@Data
public class UserResponseDTO {
    private String firebaseUid;
    private String nombre;
    private String email;
    private String celular;
    private String direccionResidencia;
    private Boolean isAdmin;
}