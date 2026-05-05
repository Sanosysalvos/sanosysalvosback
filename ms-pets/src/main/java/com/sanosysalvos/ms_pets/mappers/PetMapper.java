package com.sanosysalvos.ms_pets.mappers;

import com.sanosysalvos.ms_pets.dtos.PetRequestDTO;
import com.sanosysalvos.ms_pets.dtos.PetResponseDTO;
import com.sanosysalvos.ms_pets.models.Pet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring") // Esto permite que Spring lo inyecte en el Service con @RequiredArgsConstructor
public interface PetMapper {

    // 1. Convierte la Entidad JPA de la base de datos a un DTO de respuesta para el Frontend
    PetResponseDTO toResponseDTO(Pet pet);

    // 2. Convierte el DTO que viene del Frontend a una Entidad JPA para guardarla
    @Mapping(target = "id", ignore = true) // El ID no viene del frontend, lo genera la base de datos
    Pet toEntity(PetRequestDTO dto);

    // 3. Toma los datos nuevos de un DTO de edicion y los vuelca sobre la entidad existente de la BD
    @Mapping(target = "id", ignore = true) // El ID de la mascota nunca debe cambiar al editar
    @Mapping(target = "userUid", ignore = true) // Por seguridad, el dueño original que reporto no debería cambiar
    void updateEntityFromDto(PetRequestDTO dto, @MappingTarget Pet pet);
}