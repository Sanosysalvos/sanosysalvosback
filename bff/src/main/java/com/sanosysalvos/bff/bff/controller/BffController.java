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
@CrossOrigin(
    origins = ("http://localhost:3000"),
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
    allowedHeaders = "*"
)
public class BffController {

    @Autowired
    private BffService bffService;

    // --- PERFIL Y USUARIOS ---

    @GetMapping("/perfil/{firebaseUid}")
    public ResponseEntity<PerfilResponse> getPerfil(@PathVariable String firebaseUid) {
        try {
            PerfilResponse perfil = bffService.getPerfil(firebaseUid);
            return ResponseEntity.ok(perfil);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usuarios/firebase/{firebaseUid}")
    public ResponseEntity<Map<String, Object>> getDatosDueño(@PathVariable String firebaseUid) {
        try {
            Map<String, Object> usuario = bffService.getUsuarioPorId(firebaseUid);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/registro") // Ruta que usa tu auth de Next.js
    public ResponseEntity<Map> registrarUsuario(@RequestBody Map<String, Object> datosUsuario) {
        return bffService.registrarUsuario(datosUsuario);
    }

    @PostMapping("/users") // Ruta de respaldo
    public ResponseEntity<Map> registrar(@RequestBody Map<String, Object> datos) {
        return bffService.registrarUsuario(datos);
    }

    @PutMapping("/usuarios/{userId}")
    public ResponseEntity<Map> actualizarUsuario(@PathVariable String userId, @RequestBody Map<String, Object> datosUsuario) {
        return bffService.actualizarUsuario(userId, datosUsuario);
    }

    // --- MASCOTAS ---

    @GetMapping("/explorar")
    public ResponseEntity<List<Map<String, Object>>> explorar() {
        return ResponseEntity.ok(bffService.getMascotasParaExplorar());
    }

    @GetMapping("/mascota/{id}")
    public ResponseEntity<Map<String, Object>> getMascotaPorId(@PathVariable String id) {
        try {
            Map<String, Object> mascota = bffService.getMascotaPorId(id);
            return ResponseEntity.ok(mascota);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/reportar")
    public ResponseEntity<Map> reportarMascota(@RequestBody Map<String, Object> datosMascota) {
        return bffService.reportarMascota(datosMascota);
    }

    @PutMapping("/pets/{id}")
    public ResponseEntity<Map<String, Object>> actualizarMascota(@PathVariable String id, @RequestBody Map<String, Object> datosMascota) {
        try {
            Map<String, Object> actualizada = bffService.actualizarMascota(id, datosMascota);
            return ResponseEntity.ok(actualizada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/pets/{id}") // Añadido el slash inicial
    public ResponseEntity<Void> eliminarMascota(@PathVariable String id) {
        bffService.eliminarMascota(id); 
        return ResponseEntity.noContent().build();
    }

    // --- NOTIFICACIONES ---

    @PostMapping("/notificar-avistamiento")
    public ResponseEntity<Map<String, String>> enviarNotificacion(@RequestBody Map<String, String> payload) {
        try {
            // El payload debe traer: "petId", "nombreMascota" y "mensaje"
            bffService.enviarNotificacionAvistamiento(payload);
            return ResponseEntity.ok(Map.of("message", "Notificación enviada con éxito"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo enviar la notificación"));
        }
    }
}