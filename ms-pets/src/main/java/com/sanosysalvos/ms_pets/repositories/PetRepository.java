package com.sanosysalvos.ms_pets.repositories;

import com.sanosysalvos.ms_pets.models.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, UUID> {
    // Busca todas las mascotas asociadas a un usuario específico
    List<Pet> findByUserId(UUID userId);
}