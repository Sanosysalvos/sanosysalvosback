package com.sanosysalvos.ms_users.services;

import com.sanosysalvos.ms_users.dtos.UserRequestDTO;
import com.sanosysalvos.ms_users.dtos.UserResponseDTO;
import com.sanosysalvos.ms_users.exceptions.ResourceNotFoundException;
import com.sanosysalvos.ms_users.mappers.UserMapper;
import com.sanosysalvos.ms_users.models.User;
import com.sanosysalvos.ms_users.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                    // Actualizamos la entidad existente con los datos que vienen del DTO
                    userMapper.updateEntityFromDto(userReq, existingUser);
                    User updatedUser = userRepository.save(existingUser);
                    return userMapper.toResponseDTO(updatedUser);
                })
                .orElseGet(() -> {
                    // Mapeamos el DTO a una nueva Entidad y guardamos
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
}