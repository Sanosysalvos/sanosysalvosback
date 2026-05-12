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
<<<<<<< Updated upstream
@CrossOrigin(origins = "*") // Permite que tu front (Vercel o Local) se conecte
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Obtener todos los usuarios (Para probar)
=======
// Mantenemos CrossOrigin, pero recuerda que SecurityConfig es el que manda ahora
@CrossOrigin(origins = "https://sanosysalvos-five.vercel.app") 
public class UserController {

    @Autowired
    private UserService userService;

>>>>>>> Stashed changes
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

<<<<<<< Updated upstream
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
=======
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
>>>>>>> Stashed changes

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
<<<<<<< Updated upstream
    return ResponseEntity.notFound().build();
}
=======
    
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

>>>>>>> Stashed changes
}