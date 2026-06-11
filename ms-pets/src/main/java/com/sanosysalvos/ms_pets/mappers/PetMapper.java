package com.sanosysalvos.ms_pets.mappers;
import com.sanosysalvos.ms_pets.dtos.PetRequestDTO;
import com.sanosysalvos.ms_pets.dtos.PetResponseDTO;
import com.sanosysalvos.ms_pets.models.Pet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface PetMapper {

    // Conversión de String a UUID
    @Named("stringToUuid")
    default UUID stringToUuid(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Conversión de UUID a String (para respuestas)
    @Named("uuidToString")
    default String uuidToString(UUID value) {
        return value != null ? value.toString() : null;
    }

    // 1. Entidad → DTO (UUID a String)
    @Mapping(target = "userUid", source = "userUid", qualifiedByName = "uuidToString")
    PetResponseDTO toResponseDTO(Pet pet);

    // 2. DTO → Entidad (String a UUID)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userUid", source = "userUid", qualifiedByName = "stringToUuid")
    Pet toEntity(PetRequestDTO dto);

    // 3. Actualización (ignorar userUid por seguridad)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userUid", ignore = true)
    void updateEntityFromDto(PetRequestDTO dto, @MappingTarget Pet pet);
}