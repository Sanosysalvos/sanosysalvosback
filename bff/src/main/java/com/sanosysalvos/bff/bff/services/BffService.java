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

    // Lee las URLs desde application.properties
    @Value("${ms.users.url}")
    private String msUsersUrl;

    @Value("${ms.pets.url}")
    private String msPetsUrl;

    // -------------------------------------------------------
    // PERFIL: combina usuario + sus mascotas en una sola respuesta
    // -------------------------------------------------------
    public PerfilResponse getPerfil(String firebaseUid) {
        // 1. Llama a ms-users para obtener el usuario
        Map<String, Object> usuario = restTemplate.getForObject(
                msUsersUrl + "/api/users/firebase/" + firebaseUid,
                Map.class
        );

        // 2. Llama a ms-pets para obtener todas las mascotas
        //    (filtramos por userId en el BFF)
        List<Map<String, Object>> todasLasMascotas = restTemplate.exchange(
                msPetsUrl + "/api/pets",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        ).getBody();

        // 3. Filtra solo las mascotas del usuario
        String userId = usuario != null ? (String) usuario.get("id") : null;
        List<Map<String, Object>> mascotasDelUsuario = todasLasMascotas != null
                ? todasLasMascotas.stream()
                .filter(m -> userId != null && userId.equals(m.get("userId")))
                .toList()
                : List.of();

        // 4. Combina y retorna
        PerfilResponse perfil = new PerfilResponse();
        perfil.setUsuario(usuario);
        perfil.setMascotas(mascotasDelUsuario);
        return perfil;
    }

    // -------------------------------------------------------
    // EXPLORAR: lista todas las mascotas (para la página de explorar)
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
    // REPORTAR: recibe datos del frontend y los envía a ms-pets
    // -------------------------------------------------------
    public ResponseEntity<Map> reportarMascota(Map<String, Object> datosMascota) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosMascota, headers);
        return restTemplate.postForEntity(msPetsUrl + "/api/pets", request, Map.class);
    }

    // -------------------------------------------------------
    // REGISTRO: crea un nuevo usuario en ms-users
    // -------------------------------------------------------
    public ResponseEntity<Map> registrarUsuario(Map<String, Object> datosUsuario) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosUsuario, headers);
        return restTemplate.postForEntity(msUsersUrl + "/api/users", request, Map.class);
    }

    // -------------------------------------------------------
    // ACTUALIZAR PERFIL: actualiza datos del usuario en ms-users
    // -------------------------------------------------------
    public ResponseEntity<Map> actualizarUsuario(String userId, Map<String, Object> datosUsuario) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosUsuario, headers);
        return restTemplate.exchange(
                msUsersUrl + "/api/users/" + userId,
                HttpMethod.PUT,
                request,
                Map.class
        );
    }
}
