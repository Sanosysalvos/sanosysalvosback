package com.sanosysalvos.ms_users.controllers;

import com.sanosysalvos.ms_users.dtos.UserRequestDTO;
import com.sanosysalvos.ms_users.dtos.UserResponseDTO;
import com.sanosysalvos.ms_users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
// Mantenemos CrossOrigin, pero recuerda que SecurityConfig es el que manda ahora
@CrossOrigin(origins = {"https://sanosysalvos-five.vercel.app","http://localhost:3000"}) 
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/firebase/{uid}")
    public ResponseEntity<UserResponseDTO> getUserByFirebaseUid(@PathVariable String uid) {
        return ResponseEntity.ok(userService.getUserProfile(uid));
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> syncUser(@RequestBody UserRequestDTO userRequestDTO) {
        // Log para debug: Verifica en la consola de Docker si el DTO llega con datos
        System.out.println("Sincronizando usuario: " + userRequestDTO.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.syncUser(userRequestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID id, @RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.emailAlreadyExists(email));
    }

    @GetMapping("/check-rut")
    public ResponseEntity<Boolean> checkRut(@RequestParam String rut) {
        return ResponseEntity.ok(userService.rutAlreadyExists(rut));
    }
    // UserController.java en ms-users
@PutMapping("/firebase/{uid}")
public ResponseEntity<UserResponseDTO> updateUserByFirebaseUid(@PathVariable String uid, @RequestBody UserRequestDTO dto) {
    return ResponseEntity.ok(userService.updateUserByFirebaseUid(uid, dto));
}
    // UserController.java (ms-users)
@GetMapping("/{id}")
public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.getUserById(id));
}

}