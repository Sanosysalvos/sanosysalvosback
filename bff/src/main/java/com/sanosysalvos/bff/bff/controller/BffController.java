package com.sanosysalvos.bff.bff.controller;


import com.sanosysalvos.bff.bff.model.PerfilResponse;
import com.sanosysalvos.bff.bff.services.BffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BffController {
    @Autowired
    private BffService bffService;

    // GET /API/PERFIL
    @GetMapping("/perfil/{firebaseUid}")
    public ResponseEntity<PerfilResponse> getPerfil(@PathVariable String firebaseUid) {
        PerfilResponse perfil = bffService.getPerfil(firebaseUid);
        return ResponseEntity.ok(perfil);
    }

    // GET /API/EXPLORAR
    @GetMapping("/explorar")
    public ResponseEntity<List<Map<String, Object>>> explorar() {
        List<Map<String, Object>> mascotas = bffService.getMascotasParaExplorar();
        return ResponseEntity.ok(mascotas);
    }

    // POST /API/REPORTAR
    @PostMapping("/reportar")
    public ResponseEntity<Map> reportarMascota(@RequestBody Map<String, Object> datosMascota) {
        return bffService.reportarMascota(datosMascota);
    }

    // POST API/REGISTRO
    @PostMapping("/registro")
    public ResponseEntity<Map> registrarUsuario(@RequestBody Map<String, Object> datosUsuario) {
        return bffService.registrarUsuario(datosUsuario);
    }

    // PUT /API/USUARIOS/USUARIOID
    @PutMapping("/usuarios/{userId}")
    public ResponseEntity<Map> actualizarUsuario(
            @PathVariable String userId,
            @RequestBody Map<String, Object> datosUsuario) {
        return bffService.actualizarUsuario(userId, datosUsuario);
    }
}
