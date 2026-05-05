package com.sanosysalvos.ms_pets.dtos;

import com.sanosysalvos.ms_pets.models.PetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PetRequestDTO {

    @NotBlank(message = "El UID del usuario es obligatorio para asociar la mascota.")
    private String userUid;

    @NotBlank(message = "El nombre de la mascota no puede estar vacío.")
    private String nombre;

    @NotBlank(message = "La especie (ej: PERRO, GATO) es obligatoria.")
    private String especie;

    private String color;

    @NotNull(message = "La edad es obligatoria.")
    @PositiveOrZero(message = "La edad no puede ser un número negativo.")
    private Integer edad;

    @NotBlank(message = "Debes proporcionar una descripción detallada del extravío.")
    private String descripcion;

    @NotNull(message = "La fecha de pérdida es obligatoria.")
    private LocalDate fechaPerdida;

    @NotNull(message = "El estado inicial de la mascota es obligatorio.")
    private PetStatus estado;

    @NotNull(message = "La latitud es obligatoria para ubicar el reporte en el mapa.")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria para ubicar el reporte en el mapa.")
    private Double longitud;

    private String foto; // URL opcional al principio, o por si se sube directo a Firebase Storage
}