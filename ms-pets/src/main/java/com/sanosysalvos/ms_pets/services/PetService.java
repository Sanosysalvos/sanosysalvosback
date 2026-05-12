package com.sanosysalvos.ms_pets.services;

import com.sanosysalvos.ms_pets.dtos.PetRequestDTO;
import com.sanosysalvos.ms_pets.dtos.PetResponseDTO;
import com.sanosysalvos.ms_pets.mappers.PetMapper;
import com.sanosysalvos.ms_pets.models.Pet;
import com.sanosysalvos.ms_pets.repositories.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final PetMapper petMapper;

    @Transactional
    public PetResponseDTO crearReporte(PetRequestDTO dto) {
        Pet pet = petMapper.toEntity(dto);
        Pet guardada = petRepository.save(pet);
        return petMapper.toResponseDTO(guardada);
    }

    @Transactional(readOnly = true)
    public List<PetResponseDTO> obtenerTodas() {
        return petRepository.findAll().stream()
                .map(petMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PetResponseDTO obtenerPorId(UUID id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada con el ID: " + id));
        return petMapper.toResponseDTO(pet);
    }

    @Transactional(readOnly = true)
    public List<PetResponseDTO> obtenerPorUsuario(String userUid) {
        return petRepository.findByUserUid(userUid).stream()
                .map(petMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PetResponseDTO actualizarMascota(UUID id, PetRequestDTO dto) {
        Pet petExistente = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede actualizar. Mascota no encontrada."));

        petMapper.updateEntityFromDto(dto, petExistente);
        Pet actualizada = petRepository.save(petExistente);
        return petMapper.toResponseDTO(actualizada);
    }

    @Transactional
    public void eliminarReporte(UUID id) {
        if (!petRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. El reporte de mascota no existe.");
        }
        petRepository.deleteById(id);
    }
    
}