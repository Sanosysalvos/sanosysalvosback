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

    @Value("${ms.notification.url}")
    private String msNotificationUrl;

    @Value("${ms.sightings.url}")
    private String msSightingsUrl;

    @Value("${ms.matching.url}")
    private String msMatchingUrl;

    // -------------------------------------------------------
    // 1. PERFIL
    // -------------------------------------------------------
    public PerfilResponse getPerfil(String firebaseUid) {
        Map<String, Object> usuario = restTemplate.getForObject(
                msUsersUrl + "/api/users/firebase/" + firebaseUid, Map.class);

        PerfilResponse perfil = new PerfilResponse();
        perfil.setUsuario(usuario);

        try {
            // Usamos el endpoint por Firebase UID directamente en ms-pets
            List<Map<String, Object>> mascotasDelUsuario = restTemplate.exchange(
                    msPetsUrl + "/api/pets/owner/" + firebaseUid,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            ).getBody();
            perfil.setMascotas(mascotasDelUsuario != null ? mascotasDelUsuario : List.of());
        } catch (Exception e) {
            System.err.println("BFF: Error al traer mascotas: " + e.getMessage());
            perfil.setMascotas(List.of());
        }
        return perfil;
    }

    // -------------------------------------------------------
    // 2. EXPLORAR
    // -------------------------------------------------------
    public List<Map<String, Object>> getMascotasParaExplorar() {
        return restTemplate.exchange(
                msPetsUrl + "/api/pets",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        ).getBody();
    }

    // -------------------------------------------------------
    // 3. DETALLE MASCOTA
    // -------------------------------------------------------
    public Map<String, Object> getMascotaPorId(String id) {
        return restTemplate.getForObject(msPetsUrl + "/api/pets/" + id, Map.class);
    }

    // -------------------------------------------------------
    // 4. REPORTAR MASCOTA
    // -------------------------------------------------------
    public ResponseEntity<Map> reportarMascota(Map<String, Object> datosMascota) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosMascota, headers);
        System.out.println("BFF: Enviando a ms-pets: " + datosMascota);
        return restTemplate.postForEntity(msPetsUrl + "/api/pets", request, Map.class);
    }

    // -------------------------------------------------------
    // 5. REGISTRAR USUARIO
    // -------------------------------------------------------
    public ResponseEntity<Map> registrarUsuario(Map<String, Object> datosUsuario) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosUsuario, headers);
        return restTemplate.postForEntity(msUsersUrl + "/api/users", request, Map.class);
    }

    // -------------------------------------------------------
    // 6. ACTUALIZAR USUARIO
    // -------------------------------------------------------
    public ResponseEntity<Map> actualizarUsuario(String userId, Map<String, Object> datosUsuario) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosUsuario, headers);
        String url = msUsersUrl + "/api/users/firebase/" + userId;
        return restTemplate.exchange(url, HttpMethod.PUT, request, Map.class);
    }

    // -------------------------------------------------------
    // 7. ELIMINAR MASCOTA
    // -------------------------------------------------------
    public void eliminarMascota(String id) {
        restTemplate.delete(msPetsUrl + "/api/pets/" + id);
    }

    // -------------------------------------------------------
    // 8. ACTUALIZAR MASCOTA
    // -------------------------------------------------------
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

    // -------------------------------------------------------
    // 9. OBTENER USUARIO POR FIREBASE UID
    // -------------------------------------------------------
    public Map<String, Object> getUsuarioPorId(String uid) {
        try {
            String url = msUsersUrl + "/api/users/firebase/" + uid;
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            System.err.println("BFF: Error al obtener usuario: " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------
    // 10. NOTIFICACIÓN AVISTAMIENTO (flujo anterior)
    // -------------------------------------------------------
    public void enviarNotificacionAvistamiento(Map<String, String> payload) {
        try {
            String petId = payload.get("petId");
            String mensajeUsuario = payload.get("mensaje");

            Map<String, Object> mascota = getMascotaPorId(petId);
            if (mascota == null) {
                System.err.println("BFF: No se encontró la mascota ID: " + petId);
                return;
            }

            String ownerUid = (String) mascota.get("userUid");
            String nombreMascota = (String) mascota.get("nombre");

            Map<String, Object> dueno = getUsuarioPorId(ownerUid);
            if (dueno == null || dueno.get("email") == null) {
                System.err.println("BFF: Dueño no encontrado o sin email para UID: " + ownerUid);
                return;
            }

            String emailDueno = dueno.get("email").toString();

            Map<String, String> notificationRequest = new HashMap<>();
            notificationRequest.put("email", emailDueno);
            notificationRequest.put("mascota", nombreMascota);
            notificationRequest.put("mensaje", mensajeUsuario);

            String urlFinal = msNotificationUrl + "/api/notifications/send-test";
            System.out.println("BFF: Enviando notificación a " + urlFinal + " para: " + emailDueno);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    urlFinal, notificationRequest, String.class);
            System.out.println("BFF: Respuesta ms-notification: " + response.getStatusCode());

        } catch (Exception e) {
            System.err.println("BFF: Falló el flujo de notificación: " + e.getMessage());
            throw e;
        }
    }

    // -------------------------------------------------------
    // 11. CREAR AVISTAMIENTO + DISPARAR MATCHING
    // Flujo completo:
    //   1. Crea el avistamiento en ms-sightings
    //   2. Llama a ms-matching con el sightingId recibido
    // -------------------------------------------------------
    public Map<String, Object> crearAvistamiento(Map<String, Object> datosAvistamiento) {
        // Paso 1 — Crear avistamiento en ms-sightings
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosAvistamiento, headers);

        ResponseEntity<Map> sightingResponse = restTemplate.postForEntity(
                msSightingsUrl + "/api/sightings", request, Map.class);

        Map<String, Object> sighting = sightingResponse.getBody();
        if (sighting == null || sighting.get("id") == null) {
            throw new RuntimeException("BFF: ms-sightings no devolvió un avistamiento válido");
        }

        String sightingId = sighting.get("id").toString();
        System.out.println("BFF: Avistamiento creado con ID: " + sightingId);

        // Paso 2 — Disparar motor de matching en ms-matching
        try {
            Map<String, String> matchRequest = new HashMap<>();
            matchRequest.put("sightingId", sightingId);

            HttpEntity<Map<String, String>> matchEntity = new HttpEntity<>(matchRequest, headers);
            ResponseEntity<Map> matchResponse = restTemplate.postForEntity(
                    msMatchingUrl + "/api/matching/calcular", matchEntity, Map.class);

            Map<String, Object> matchResult = matchResponse.getBody();
            System.out.println("BFF: Resultado matching: " + matchResult);

            // Enriquecer la respuesta con el resultado del matching
            sighting.put("matching", matchResult);

        } catch (Exception e) {
            // Si falla el matching no bloqueamos — el avistamiento ya fue guardado
            System.err.println("BFF: Error en matching (no crítico): " + e.getMessage());
            sighting.put("matching", Map.of(
                "mensaje", "Avistamiento guardado. Motor de coincidencias no disponible."
            ));
        }

        return sighting;
    }

    // -------------------------------------------------------
    // 12. OBTENER AVISTAMIENTOS DE UNA MASCOTA
    // -------------------------------------------------------
    public List<Map<String, Object>> getAvistamientosPorMascota(String petId) {
        try {
            return restTemplate.exchange(
                    msSightingsUrl + "/api/sightings/pet/" + petId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            ).getBody();
        } catch (Exception e) {
            System.err.println("BFF: Error al obtener avistamientos: " + e.getMessage());
            return List.of();
        }
    }

    // -------------------------------------------------------
    // 13. OBTENER AVISTAMIENTOS DE UN USUARIO
    // -------------------------------------------------------
    public List<Map<String, Object>> getAvistamientosPorUsuario(String uid) {
        try {
            return restTemplate.exchange(
                    msSightingsUrl + "/api/sightings/user/" + uid,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            ).getBody();
        } catch (Exception e) {
            System.err.println("BFF: Error al obtener avistamientos del usuario: " + e.getMessage());
            return List.of();
        }
    }
}