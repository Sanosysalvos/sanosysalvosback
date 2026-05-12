package com.sanosysalvos.ms_users.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserRequestDTO {
    
    @JsonProperty("firebase_uid") // ESTO vincula el JSON del front con esta variable
    private String firebaseUid;
    
    private String nombre;
    private String email;
    private String celular;
    private String direccionResidencia;
    
    private String rut;
}