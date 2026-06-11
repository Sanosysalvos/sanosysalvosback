package com.sanosysalvos.ms_pets.controllers;

import com.sanosysalvos.ms_pets.dtos.PetRequestDTO;
import com.sanosysalvos.ms_pets.dtos.PetResponseDTO;
import com.sanosysalvos.ms_pets.services.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permite la conexion directa con Next.js
public class PetController {

    private final PetService petService;


    @PostMapping
    public ResponseEntity<PetResponseDTO> crearReporte(@Valid @RequestBody PetRequestDTO dto) {
        PetResponseDTO nuevaMascota = petService.crearReporte(dto);
        return new ResponseEntity<>(nuevaMascota, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PetResponseDTO>> obtenerTodas(
    @RequestParam(value = "recientes", required = false, defaultValue = "false") boolean recientes
    ) 
    {
    // Le pasamos el booleano al servicio para que decida qué traer
    return ResponseEntity.ok(petService.obtenerTodas(recientes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(petService.obtenerPorId(id));
    }

    @GetMapping("/user/{userUid}")
    public ResponseEntity<List<PetResponseDTO>> obtenerPorUsuario(@PathVariable String userUid) {
        return ResponseEntity.ok(petService.obtenerPorUsuario(userUid));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponseDTO> actualizarMascota(
            @PathVariable UUID id,
            @Valid @RequestBody PetRequestDTO dto) {
        return ResponseEntity.ok(petService.actualizarMascota(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReporte(@PathVariable UUID id) {
        petService.eliminarReporte(id);
        return ResponseEntity.noContent().build();
    }
}