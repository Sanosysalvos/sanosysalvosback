package com.sanosysalvos.ms_users.controllers;

import com.sanosysalvos.ms_users.models.User;
import com.sanosysalvos.ms_users.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Permite que tu front (Vercel o Local) se conecte
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Obtener todos los usuarios (Para probar)
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Buscar un usuario por su Firebase UID (Lo que necesita tu front)
    @GetMapping("/firebase/{uid}")
    public ResponseEntity<User> getUserByFirebaseUid(@PathVariable String uid) {
        return userRepository.findByFirebaseUid(uid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear un nuevo usuario (Para el registro)
    // 1. CREATE - Crear un usuario (POST)
@PostMapping
public ResponseEntity<User> createUser(@RequestBody User user) {
    User savedUser = userRepository.save(user);
    return ResponseEntity.status(201).body(savedUser);
}

// 2. UPDATE - Actualizar datos (PUT)
@PutMapping("/{id}")
public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User userDetails) {
  // En UserController.java, dentro de updateUser:
return userRepository.findById(id).map(user -> {
    if(userDetails.getNombre() != null) user.setNombre(userDetails.getNombre());
    if(userDetails.getEmail() != null) user.setEmail(userDetails.getEmail());
    if(userDetails.getCelular() != null) user.setCelular(userDetails.getCelular());
    if(userDetails.getDireccionResidencia() != null) user.setDireccionResidencia(userDetails.getDireccionResidencia());
    // Mantenemos el firebaseUid original si no viene uno nuevo
    return ResponseEntity.ok(userRepository.save(user));
}).orElse(ResponseEntity.notFound().build());
}

// 3. DELETE - Borrar usuario (DELETE)
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
    if (userRepository.existsById(id)) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
}
}