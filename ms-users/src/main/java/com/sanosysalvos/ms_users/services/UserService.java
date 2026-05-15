package com.sanosysalvos.ms_users.services;

import com.sanosysalvos.ms_users.dtos.UserRequestDTO;
import com.sanosysalvos.ms_users.dtos.UserResponseDTO;
import com.sanosysalvos.ms_users.exceptions.ResourceNotFoundException;
import com.sanosysalvos.ms_users.mappers.UserMapper;
import com.sanosysalvos.ms_users.models.User;
import com.sanosysalvos.ms_users.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserProfile(String firebaseUid) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con Firebase UID: " + firebaseUid));
        return userMapper.toResponseDTO(user);
    }


    public UserResponseDTO syncUser(UserRequestDTO userReq) {
        return userRepository.findByFirebaseUid(userReq.getFirebaseUid())
                .map(existingUser -> {
                    return userMapper.toResponseDTO(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = userMapper.toEntity(userReq);
                    User savedUser = userRepository.save(newUser);
                    return userMapper.toResponseDTO(savedUser);
                });
    }


    public UserResponseDTO updateUser(UUID id, UserRequestDTO userDetailsDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se pudo actualizar: Usuario no encontrado con ID: " + id));

        // El mapper sobreescribe solo los campos presentes en el DTO
        userMapper.updateEntityFromDto(userDetailsDTO, user);

        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDTO(updatedUser);
    }


    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se pudo eliminar: Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }

    public boolean emailAlreadyExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean rutAlreadyExists(String rut) {
        return userRepository.existsByRut(rut);
    }
    
@Transactional
public UserResponseDTO updateUserByFirebaseUid(String uid, UserRequestDTO dto) {
    // 1. Buscamos al usuario real
    User user = userRepository.findByFirebaseUid(uid)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con UID: " + uid));

    // 2. LOG DE SEGURIDAD (Esto lo verás en Docker ms-users)
    System.out.println("DEBUG: Intentando actualizar usuario " + user.getEmail());

    // 3. Mapeo manual o controlado para evitar pisar campos obligatorios con nulls
    if (dto.getNombre() != null) user.setNombre(dto.getNombre());
    if (dto.getCelular() != null) user.setCelular(dto.getCelular());
    if (dto.getDireccionResidencia() != null) user.setDireccionResidencia(dto.getDireccionResidencia());
    if (dto.getRut() != null) user.setRut(dto.getRut());
    
    // El email suele ser delicado, lo actualizamos solo si viene en el DTO
    if (dto.getEmail() != null) user.setEmail(dto.getEmail());

    // 4. Guardar
    try {
        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDTO(updatedUser);
    } catch (Exception e) {
        // Esto atrapará errores de base de datos (como duplicados de RUT/Email)
        System.err.println("ERROR DE PERSISTENCIA: " + e.getMessage());
        throw new RuntimeException("Error al guardar en base de datos: " + e.getMessage());
    }
}
public UserResponseDTO getUserById(UUID id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    return userMapper.toResponseDTO(user);
}
}