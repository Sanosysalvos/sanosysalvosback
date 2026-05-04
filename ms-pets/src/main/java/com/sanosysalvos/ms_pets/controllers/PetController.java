package com.sanosysalvos.ms_pets.controllers;

import com.sanosysalvos.ms_pets.models.Pet;
import com.sanosysalvos.ms_pets.repositories.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
@CrossOrigin(origins = "*")
public class PetController {

    @Autowired
    private PetRepository petRepository;

    // Listar todas las mascotas
    @GetMapping
    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    // Crear una mascota (POST)
    @PostMapping
    public ResponseEntity<Pet> createPet(@RequestBody Pet pet) {
        Pet savedPet = petRepository.save(pet);
        return ResponseEntity.status(201).body(savedPet);
    }

    // Eliminar mascota (Por si te equivocas en una prueba)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable UUID id) {
        if (petRepository.existsById(id)) {
            petRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
@PutMapping("/{id}")
public ResponseEntity<Pet> updatePet(@PathVariable UUID id, @RequestBody Pet petDetails) {
    return petRepository.findById(id).map(pet -> {
        pet.setNombre(petDetails.getNombre());
        pet.setEspecie(petDetails.getEspecie()); // Coincide con tu nuevo script
        pet.setColor(petDetails.getColor());
        pet.setEdad(petDetails.getEdad());
        pet.setDescripcion(petDetails.getDescripcion());
        pet.setFechaPerdida(petDetails.getFechaPerdida());
        pet.setEstado(petDetails.getEstado());
        pet.setLatitud(petDetails.getLatitud());
        pet.setLongitud(petDetails.getLongitud());
        pet.setFoto(petDetails.getFoto());
        return ResponseEntity.ok(petRepository.save(pet));
    }).orElse(ResponseEntity.notFound().build());
}
}