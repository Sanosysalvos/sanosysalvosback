package com.sanosysalvos.bff.bff.controller;

import com.sanosysalvos.bff.bff.model.PerfilResponse;
import com.sanosysalvos.bff.bff.services.BffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
// Es mejor apuntar directamente a tu puerto de Next.js
@CrossOrigin(origins = "https://sanosysalvos-five.vercel.app",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
) 
public class BffController {

    @Autowired
    private BffService bffService;

    // GET /API/PERFIL/{firebaseUid}
    // Este es el que usará tu nueva página de Perfil dinámica
    @GetMapping("/perfil/{firebaseUid}")
    public ResponseEntity<PerfilResponse> getPerfil(@PathVariable String firebaseUid) {
        try {
            PerfilResponse perfil = bffService.getPerfil(firebaseUid);
            return ResponseEntity.ok(perfil);
        } catch (Exception e) {
            // Si ms-users o ms-pets fallan, devolvemos un error controlado
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /API/EXPLORAR
    @GetMapping("/explorar")
    public ResponseEntity<List<Map<String, Object>>> explorar() {
        return ResponseEntity.ok(bffService.getMascotasParaExplorar());
    }

    // GET /API/MASCOTA/{id}
    @GetMapping("/mascota/{id}")
    public ResponseEntity<Map<String, Object>> getMascotaPorId(@PathVariable String id) {
        try {
            Map<String, Object> mascota = bffService.getMascotaPorId(id);
            return ResponseEntity.ok(mascota);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // POST /API/REPORTAR
    @PostMapping("/reportar")
    public ResponseEntity<Map> reportarMascota(@RequestBody Map<String, Object> datosMascota) {
        return bffService.reportarMascota(datosMascota);
    }

    // POST /API/REGISTRO
    @PostMapping("/registro")
    public ResponseEntity<Map> registrarUsuario(@RequestBody Map<String, Object> datosUsuario) {
        return bffService.registrarUsuario(datosUsuario);
    }

    // PUT /API/USUARIOS/{userId}
    @PutMapping("/usuarios/{userId}")
    public ResponseEntity<Map> actualizarUsuario(
            @PathVariable String userId,
            @RequestBody Map<String, Object> datosUsuario) {
        return bffService.actualizarUsuario(userId, datosUsuario);
    }
    // En tu Controller del BFF (Puerto 8080)
    @DeleteMapping("pets/{id}")
        public ResponseEntity<Void> eliminarMascota(@PathVariable String id) {
        // Aquí llamas al método de tu service que usa el restTemplate
            // Si tu service no tiene el método eliminar, hay que crearlo abajo
    bffService.eliminarMascota(id); 
    return ResponseEntity.noContent().build();
}
// PUT /API/PETS/{id}
    @PutMapping("/pets/{id}")
    public ResponseEntity<Map<String, Object>> actualizarMascota(
            @PathVariable String id,
            @RequestBody Map<String, Object> datosMascota) {
        try {
            // Llamamos al service para que haga el puente con el microservicio de mascotas
            Map<String, Object> actualizada = bffService.actualizarMascota(id, datosMascota);
            return ResponseEntity.ok(actualizada);
        } catch (Exception e) {
            System.err.println("Error al actualizar mascota en BFF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
// Este es específico para el Modal del Administrador
@GetMapping("/usuarios/firebase/{firebaseUid}")
public ResponseEntity<Map<String, Object>> getDatosDueño(@PathVariable String firebaseUid) {
    try {
        // Llamamos al service para que vaya a ms-users/api/users/firebase/UID
        Map<String, Object> usuario = bffService.getUsuarioPorId(firebaseUid);
        return ResponseEntity.ok(usuario);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
// 1. REGISTRO: Recibe de Vercel y manda a ms-users
@PostMapping("/users")
public ResponseEntity<Map> registrar(@RequestBody Map<String, Object> datos) {
    // El microservicio devuelve el usuario creado, el BFF debe pasarlo de vuelta al frontend
    return bffService.registrarUsuario(datos);
}
}