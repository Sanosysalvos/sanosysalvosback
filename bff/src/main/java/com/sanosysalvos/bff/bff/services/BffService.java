package com.sanosysalvos.bff.bff.services;

import com.sanosysalvos.bff.bff.model.PerfilResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class BffService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ms.users.url}")
    private String msUsersUrl;

    @Value("${ms.pets.url}")
    private String msPetsUrl;

    // --- 1. PERFIL (MEJORADO: Directo por UID para no traer miles de mascotas) ---
    public PerfilResponse getPerfil(String firebaseUid) {
        // Obtenemos el usuario de ms-users
        Map<String, Object> usuario = restTemplate.getForObject(
                msUsersUrl + "/api/users/firebase/" + firebaseUid,
                Map.class
        );

        // Pedimos directamente a ms-pets las mascotas de este dueño
        // (Asegúrate de tener el endpoint GET /api/pets/owner/{uid} en ms-pets)
        List<Map<String, Object>> mascotasDelUsuario = restTemplate.exchange(
                msPetsUrl + "/api/pets/owner/" + firebaseUid,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        ).getBody();

        PerfilResponse perfil = new PerfilResponse();
        perfil.setUsuario(usuario);
        perfil.setMascotas(mascotasDelUsuario != null ? mascotasDelUsuario : List.of());
        return perfil;
    }

    // --- 2. EXPLORAR: Trae TODO el catálogo de ms-pets ---
    public List<Map<String, Object>> getMascotasParaExplorar() {
        return restTemplate.exchange(
                msPetsUrl + "/api/pets",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        ).getBody();
    }

    // --- 3. DETALLE: Busca una mascota específica por ID ---
    public Map<String, Object> getMascotaPorId(String id) {
        return restTemplate.getForObject(msPetsUrl + "/api/pets/" + id, Map.class);
    }

    // --- 4. REPORTAR: Proxy para guardar en ms-pets ---
    public ResponseEntity<Map> reportarMascota(Map<String, Object> datosMascota) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosMascota, headers);
        return restTemplate.postForEntity(msPetsUrl + "/api/pets", request, Map.class);
    }

    // --- 5. REGISTRO: Proxy para crear usuario en ms-users ---
    public ResponseEntity<Map> registrarUsuario(Map<String, Object> datosUsuario) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosUsuario, headers);
        return restTemplate.postForEntity(msUsersUrl + "/api/users", request, Map.class);
    }

    // --- 6. ACTUALIZAR: Proxy para modificar en ms-users ---
  // BffService.java
public ResponseEntity<Map> actualizarUsuario(String userId, Map<String, Object> datosUsuario) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosUsuario, headers);

    // CORRECCIÓN AQUÍ: Agregamos "/firebase/" a la ruta
    String url = msUsersUrl + "/api/users/firebase/" + userId; 

    return restTemplate.exchange(
            url,
            HttpMethod.PUT,
            request,
            Map.class
    );
}
    public void eliminarMascota(String id) {
        // Esto envía un DELETE al microservicio de mascotas
        // La URL final será algo como: http://localhost:8082/api/pets/123e4567-e89b...
        restTemplate.delete(msPetsUrl + "/api/pets/" + id);
    }
    // --- 7. ACTUALIZAR MASCOTA: Proxy para modificar en ms-pets ---
    public Map<String, Object> actualizarMascota(String id, Map<String, Object> datosMascota) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosMascota, headers);
        
        // Usamos exchange porque restTemplate.put no devuelve el cuerpo de la respuesta
        return restTemplate.exchange(
                msPetsUrl + "/api/pets/" + id,
                HttpMethod.PUT,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        ).getBody();
    }
// --- 8. BUSCAR DUEÑO (Para Panel Admin): Usa el Firebase UID que viene de ms-pets ---
public Map<String, Object> getUsuarioPorId(String uid) {
    try {
        // 1. Usamos la variable msUsersUrl que ya tienes inyectada
        // 2. Apuntamos a /firebase/ porque el UID que recibimos es el de Firebase (zHAj2...)
        String url = msUsersUrl + "/api/users/firebase/" + uid; 
        
        System.out.println("BFF consultando dueño en: " + url);
        
        return restTemplate.getForObject(url, Map.class);
    } catch (Exception e) {
        System.err.println("Error en BFF al obtener usuario por Firebase UID: " + e.getMessage());
        // Devolvemos null para que el Controller pueda manejar el 404
        return null; 
    }
}

}