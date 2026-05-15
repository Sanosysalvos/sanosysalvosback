package com.sanosysalvos.bff.bff.services;

import com.sanosysalvos.bff.bff.model.PerfilResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
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

    // NUEVA VARIABLE: Inyectada desde application.properties
    @Value("${ms.notification.url}")
    private String msNotificationUrl;

    // --- 1. PERFIL ---
    public PerfilResponse getPerfil(String firebaseUid) {
        Map<String, Object> usuario = restTemplate.getForObject(
                msUsersUrl + "/api/users/firebase/" + firebaseUid, Map.class);

        PerfilResponse perfil = new PerfilResponse();
        perfil.setUsuario(usuario);

        try {
            List<Map<String, Object>> mascotasDelUsuario = restTemplate.exchange(
                    msPetsUrl + "/api/pets/owner/" + firebaseUid,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            ).getBody();
            perfil.setMascotas(mascotasDelUsuario != null ? mascotasDelUsuario : List.of());
        } catch (Exception e) {
            System.err.println("Error al traer mascotas: " + e.getMessage());
            perfil.setMascotas(List.of());
        }
        return perfil;
    }

    // --- 2. EXPLORAR ---
    public List<Map<String, Object>> getMascotasParaExplorar() {
        return restTemplate.exchange(
                msPetsUrl + "/api/pets",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        ).getBody();
    }

    // --- 3. DETALLE ---
    public Map<String, Object> getMascotaPorId(String id) {
        return restTemplate.getForObject(msPetsUrl + "/api/pets/" + id, Map.class);
    }

    // --- 4. REPORTAR ---
    public ResponseEntity<Map> reportarMascota(Map<String, Object> datosMascota) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosMascota, headers);
        return restTemplate.postForEntity(msPetsUrl + "/api/pets", request, Map.class);
    }

    // --- 5. REGISTRO ---
    public ResponseEntity<Map> registrarUsuario(Map<String, Object> datosUsuario) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosUsuario, headers);
        return restTemplate.postForEntity(msUsersUrl + "/api/users", request, Map.class);
    }

    // --- 6. ACTUALIZAR USUARIO ---
    public ResponseEntity<Map> actualizarUsuario(String userId, Map<String, Object> datosUsuario) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosUsuario, headers);
        String url = msUsersUrl + "/api/users/firebase/" + userId; 
        return restTemplate.exchange(url, HttpMethod.PUT, request, Map.class);
    }

    public void eliminarMascota(String id) {
        restTemplate.delete(msPetsUrl + "/api/pets/" + id);
    }

    // --- 7. ACTUALIZAR MASCOTA ---
    public Map<String, Object> actualizarMascota(String id, Map<String, Object> datosMascota) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosMascota, headers);
        return restTemplate.exchange(
                msPetsUrl + "/api/pets/" + id,
                HttpMethod.PUT,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        ).getBody();
    }

    // --- 8. BUSCAR DUEÑO POR UID ---
    public Map<String, Object> getUsuarioPorId(String uid) {
        try {
            String url = msUsersUrl + "/api/users/firebase/" + uid; 
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            System.err.println("Error en BFF al obtener usuario: " + e.getMessage());
            return null; 
        }
    }

    // --- 9. NOTIFICACIÓN (CORREGIDO Y DINÁMICO) ---
    public void enviarNotificacionAvistamiento(Map<String, String> payload) {
        try {
            String petId = payload.get("petId");
            String mensajeUsuario = payload.get("mensaje");

            // 1. Obtener la mascota (Usamos userUid que es el campo real del DTO)
            Map<String, Object> mascota = getMascotaPorId(petId);
            if (mascota == null) {
                System.err.println("BFF: No se encontró la mascota ID: " + petId);
                return;
            }

            String ownerUid = (String) mascota.get("userUid"); 
            String nombreMascota = (String) mascota.get("nombre");

            // 2. Obtener datos del dueño para el email
            Map<String, Object> dueño = getUsuarioPorId(ownerUid);
            if (dueño == null || dueño.get("email") == null) {
                System.err.println("BFF: Dueño no encontrado o sin email para UID: " + ownerUid);
                return; 
            }

            String emailDueño = dueño.get("email").toString();

            // 3. Preparar JSON para ms-notification
            Map<String, String> notificationRequest = new HashMap<>();
            notificationRequest.put("email", emailDueño);
            notificationRequest.put("mascota", nombreMascota);
            notificationRequest.put("mensaje", mensajeUsuario);

            // 4. URL DINÁMICA (Usa la variable msNotificationUrl)
            String urlFinal = msNotificationUrl + "/api/notifications/send-test"; 
            
            System.out.println("BFF: Enviando notificación a " + urlFinal + " para: " + emailDueño);
            
            ResponseEntity<String> response = restTemplate.postForEntity(urlFinal, notificationRequest, String.class);
            
            System.out.println("BFF: Respuesta ms-notification: " + response.getStatusCode());

        } catch (Exception e) {
            System.err.println("BFF: Falló el flujo de notificación: " + e.getMessage());
            throw e;
        }
    }
}