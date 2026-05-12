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
@CrossOrigin(origins = "https://sanosysalvos-five.vercel.app")
public class PetController {

    private final PetService petService;


    @PostMapping
    public ResponseEntity<PetResponseDTO> crearReporte(@Valid @RequestBody PetRequestDTO dto) {
        PetResponseDTO nuevaMascota = petService.crearReporte(dto);
        return new ResponseEntity<>(nuevaMascota, HttpStatus.CREATED);
    }
    // En PetController.java (Microservicio ms-pets)
        @GetMapping("/owner/{uid}")
        public ResponseEntity<List<PetResponseDTO>> getByOwner(@PathVariable String uid) {
    // Aquí llamas al método del service que ya vimos
        return ResponseEntity.ok(petService.obtenerPorUsuario(uid));
}
    @GetMapping
    public ResponseEntity<List<PetResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(petService.obtenerTodas());
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