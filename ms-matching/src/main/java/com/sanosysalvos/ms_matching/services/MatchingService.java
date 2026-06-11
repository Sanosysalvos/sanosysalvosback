package com.sanosysalvos.ms_matching.services;

import com.sanosysalvos.ms_matching.dtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final RestTemplate restTemplate;

    @Value("${ms.pets.url}")
    private String msPetsUrl;

    @Value("${ms.sightings.url}")
    private String msSightingsUrl;

    @Value("${ms.notification.url}")
    private String msNotificationUrl;

    @Value("${ms.users.url}")
    private String msUsersUrl;

    @Value("${matching.umbral.notificacion}")
    private double umbralNotificacion;

    @Value("${matching.radio.km}")
    private double radioKm;

    // Pesos del motor de coincidencias (deben sumar 100)
    private static final double PESO_DISTANCIA    = 35.0;
    private static final double PESO_DESCRIPCION  = 35.0;
    private static final double PESO_ESPECIE      = 30.0;

    // ---------------------------------------------------------------
    // MÉTODO PRINCIPAL
    // ---------------------------------------------------------------
    public MatchResultDTO calcularCoincidencia(String sightingId) {
        log.info("Iniciando motor de coincidencias para sightingId: {}", sightingId);

        // 1. Obtener el avistamiento desde ms-sightings
        SightingDTO sighting = obtenerAvistamiento(sightingId);
        if (sighting == null) {
            log.error("No se encontró el avistamiento: {}", sightingId);
            return resultadoError(sightingId, "Avistamiento no encontrado");
        }

        // 2. Obtener la mascota perdida desde ms-pets
        PetDTO pet = obtenerMascota(sighting.getPetId());
        if (pet == null) {
            log.error("No se encontró la mascota: {}", sighting.getPetId());
            return resultadoError(sightingId, "Mascota no encontrada");
        }

        // 3. Calcular cada componente del score
        double scoreDistancia   = calcularScoreDistancia(sighting, pet);
        double scoreDescripcion = calcularScoreDescripcion(sighting, pet);
        double scoreEspecie     = calcularScoreEspecie(sighting, pet);

        // 4. Calcular porcentaje total ponderado
        double porcentajeTotal =
            (scoreDistancia   * PESO_DISTANCIA   / 100.0) +
            (scoreDescripcion * PESO_DESCRIPCION / 100.0) +
            (scoreEspecie     * PESO_ESPECIE     / 100.0);

        // Redondear a 2 decimales
        porcentajeTotal = redondear(porcentajeTotal);

        log.info("Resultado matching — Total: {}% | Distancia: {}% | Descripción: {}% | Especie: {}%",
                porcentajeTotal, redondear(scoreDistancia * PESO_DISTANCIA / 100.0),
                redondear(scoreDescripcion * PESO_DESCRIPCION / 100.0),
                redondear(scoreEspecie * PESO_ESPECIE / 100.0));

        // 5. Actualizar el avistamiento con el resultado
        boolean notificacionEnviada = false;
        boolean umbralSuperado = porcentajeTotal >= umbralNotificacion;

        actualizarAvistamiento(sightingId, porcentajeTotal, false);

        // 6. Si supera el umbral, notificar al dueño
        if (umbralSuperado) {
            log.info("Umbral superado ({}% >= {}%). Enviando notificación al dueño.",
                    porcentajeTotal, umbralNotificacion);
            notificacionEnviada = enviarNotificacion(pet, sighting, porcentajeTotal);

            // Actualizar con notificación enviada
            actualizarAvistamiento(sightingId, porcentajeTotal, notificacionEnviada);
        }

        // 7. Construir respuesta
        String mensaje = construirMensaje(porcentajeTotal, umbralSuperado, notificacionEnviada);

        return new MatchResultDTO(
            sightingId,
            sighting.getPetId(),
            porcentajeTotal,
            redondear(scoreDistancia   * PESO_DISTANCIA   / 100.0),
            redondear(scoreDescripcion * PESO_DESCRIPCION / 100.0),
            redondear(scoreEspecie     * PESO_ESPECIE     / 100.0),
            umbralSuperado,
            notificacionEnviada,
            mensaje
        );
    }

    // ---------------------------------------------------------------
    // COMPONENTE 1: DISTANCIA (35%)
    // Fórmula Haversine — distancia real entre dos puntos GPS
    // Score 100 si distancia = 0km, Score 0 si distancia >= radioKm
    // ---------------------------------------------------------------
    private double calcularScoreDistancia(SightingDTO sighting, PetDTO pet) {
        if (pet.getLatitud() == null || pet.getLongitud() == null ||
            sighting.getLatitud() == null || sighting.getLongitud() == null) {
            log.warn("Coordenadas faltantes — score distancia = 0");
            return 0.0;
        }

        double distanciaKm = calcularDistanciaHaversine(
            pet.getLatitud(),    pet.getLongitud(),
            sighting.getLatitud(), sighting.getLongitud()
        );

        log.info("Distancia calculada: {} km (radio máximo: {} km)", 
                redondear(distanciaKm), radioKm);

        if (distanciaKm >= radioKm) return 0.0;

        // Interpolación lineal inversa: más cerca = más score
        return ((radioKm - distanciaKm) / radioKm) * 100.0;
    }

    // ---------------------------------------------------------------
    // COMPONENTE 2: DESCRIPCIÓN (35%)
    // Similitud de Jaccard sobre palabras clave
    // ---------------------------------------------------------------
    private double calcularScoreDescripcion(SightingDTO sighting, PetDTO pet) {
        String descPet      = normalizar(pet.getDescripcion());
        String descSighting = normalizar(sighting.getComentario());

        if (descPet.isEmpty() || descSighting.isEmpty()) {
            log.warn("Descripción vacía — score descripción = 0");
            return 0.0;
        }

        Set<String> palabrasPet      = new HashSet<>(Arrays.asList(descPet.split("\\s+")));
        Set<String> palabrasSighting = new HashSet<>(Arrays.asList(descSighting.split("\\s+")));

        // Eliminar stopwords en español
        eliminarStopwords(palabrasPet);
        eliminarStopwords(palabrasSighting);

        if (palabrasPet.isEmpty() || palabrasSighting.isEmpty()) return 0.0;

        // Intersección / Unión = similitud Jaccard
        Set<String> interseccion = new HashSet<>(palabrasPet);
        interseccion.retainAll(palabrasSighting);

        Set<String> union = new HashSet<>(palabrasPet);
        union.addAll(palabrasSighting);

        double similitud = (double) interseccion.size() / union.size();
        log.info("Similitud descripción (Jaccard): {} | Palabras comunes: {}",
                redondear(similitud * 100), interseccion);

        return similitud * 100.0;
    }

    // ---------------------------------------------------------------
    // COMPONENTE 3: ESPECIE (30%)
    // Coincidencia exacta = 100, no coincide = 0
    // ---------------------------------------------------------------
    private double calcularScoreEspecie(SightingDTO sighting, PetDTO pet) {
        // El avistamiento no tiene especie propia — la especie viene
        // del comentario del avistamiento, busca si menciona la especie
        String comentario = normalizar(sighting.getComentario());
        String especie    = normalizar(pet.getEspecie());

        if (especie.isEmpty()) return 0.0;

        // Si el comentario menciona la especie de la mascota = coincidencia total
        if (comentario.contains(especie)) {
            log.info("Especie '{}' mencionada en el avistamiento — score: 100", especie);
            return 100.0;
        }

        // Sinónimos comunes
        Map<String, List<String>> sinonimos = new HashMap<>();
        sinonimos.put("perro", Arrays.asList("can", "canino", "cachorro", "perrito", "mascota"));
        sinonimos.put("gato",  Arrays.asList("felino", "gatito", "michi", "minino", "mascota"));

        List<String> aliases = sinonimos.getOrDefault(especie, Collections.emptyList());
        for (String alias : aliases) {
            if (comentario.contains(alias)) {
                log.info("Sinónimo de especie '{}' encontrado — score: 80", especie);
                return 80.0;
            }
        }

        log.info("Especie no mencionada en avistamiento — score: 0");
        return 0.0;
    }

    // ---------------------------------------------------------------
    // FÓRMULA HAVERSINE — distancia entre dos puntos GPS en km
    // ---------------------------------------------------------------
    private double calcularDistanciaHaversine(double lat1, double lon1,
                                               double lat2, double lon2) {
        final double R = 6371.0; // Radio de la Tierra en km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // ---------------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------------
    private String normalizar(String texto) {
        if (texto == null) return "";
        return texto.toLowerCase()
                    .replaceAll("[áàä]", "a")
                    .replaceAll("[éèë]", "e")
                    .replaceAll("[íìï]", "i")
                    .replaceAll("[óòö]", "o")
                    .replaceAll("[úùü]", "u")
                    .replaceAll("[^a-z0-9\\s]", " ")
                    .trim();
    }

    private void eliminarStopwords(Set<String> palabras) {
        Set<String> stopwords = new HashSet<>(Arrays.asList(
            "el", "la", "los", "las", "un", "una", "unos", "unas",
            "de", "del", "al", "en", "con", "por", "para", "que",
            "es", "era", "fue", "ser", "estar", "tiene", "tenia",
            "y", "o", "a", "su", "sus", "mi", "me", "lo", "se",
            "muy", "mas", "pero", "como", "cuando", "donde", "este",
            "esta", "esto", "ese", "esa", "lo", "le", "les"
        ));
        palabras.removeAll(stopwords);
        palabras.removeIf(p -> p.length() <= 2);
    }

    private double redondear(double valor) {
        return BigDecimal.valueOf(valor)
                         .setScale(2, RoundingMode.HALF_UP)
                         .doubleValue();
    }

    private String construirMensaje(double porcentaje, boolean umbral, boolean notificado) {
        if (!umbral) {
            return String.format(
                "Coincidencia del %.1f%% — por debajo del umbral mínimo (%.0f%%). No se envió notificación.",
                porcentaje, umbralNotificacion);
        }
        if (notificado) {
            return String.format(
                "Coincidencia del %.1f%% — umbral superado. Notificación enviada al dueño.",
                porcentaje);
        }
        return String.format(
            "Coincidencia del %.1f%% — umbral superado, pero falló el envío de notificación.",
            porcentaje);
    }

    private MatchResultDTO resultadoError(String sightingId, String mensaje) {
        return new MatchResultDTO(sightingId, null, 0, 0, 0, 0,
                false, false, "ERROR: " + mensaje);
    }

    // ---------------------------------------------------------------
    // LLAMADAS A OTROS MICROSERVICIOS
    // ---------------------------------------------------------------
    private SightingDTO obtenerAvistamiento(String sightingId) {
        try {
            String url = msSightingsUrl + "/api/sightings/" + sightingId;
            ResponseEntity<SightingDTO> response =
                restTemplate.getForEntity(url, SightingDTO.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error al obtener avistamiento {}: {}", sightingId, e.getMessage());
            return null;
        }
    }

    private PetDTO obtenerMascota(String petId) {
        try {
            String url = msPetsUrl + "/api/pets/" + petId;
            ResponseEntity<PetDTO> response =
                restTemplate.getForEntity(url, PetDTO.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error al obtener mascota {}: {}", petId, e.getMessage());
            return null;
        }
    }

    private UserDTO obtenerDueno(String firebaseUid) {
        try {
            String url = msUsersUrl + "/api/users/firebase/" + firebaseUid;
            ResponseEntity<UserDTO> response =
                restTemplate.getForEntity(url, UserDTO.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error al obtener dueño {}: {}", firebaseUid, e.getMessage());
            return null;
        }
    }

    private void actualizarAvistamiento(String sightingId, double porcentaje,
                                         boolean notificado) {
        try {
            String url = msSightingsUrl + "/api/sightings/" + sightingId + "/matching";
            Map<String, Object> payload = new HashMap<>();
            payload.put("porcentaje", porcentaje);
            payload.put("notificacionEnviada", notificado);
            restTemplate.put(url, payload);
        } catch (Exception e) {
            log.error("Error al actualizar avistamiento {}: {}", sightingId, e.getMessage());
        }
    }

    private boolean enviarNotificacion(PetDTO pet, SightingDTO sighting, double porcentaje) {
        try {
            // Obtener email del dueño
            UserDTO dueno = obtenerDueno(pet.getUserUid());
            if (dueno == null || dueno.getEmail() == null) {
                log.error("No se pudo obtener el email del dueño para mascota: {}", pet.getId());
                return false;
            }

            String mensaje = String.format(
                "¡Alguien avistó a tu mascota %s! " +
                "Motor de coincidencias: %.1f%%. " +
                "Ubicación del avistamiento: lat %.6f, lng %.6f. " +
                "Comentario: %s",
                pet.getNombre(),
                porcentaje,
                sighting.getLatitud(),
                sighting.getLongitud(),
                sighting.getComentario() != null ? sighting.getComentario() : "Sin comentario"
            );

            String url = msNotificationUrl + "/api/notifications/send-test";
            Map<String, String> payload = new HashMap<>();
            payload.put("email",   dueno.getEmail());
            payload.put("mascota", pet.getNombre());
            payload.put("mensaje", mensaje);

            restTemplate.postForEntity(url, payload, String.class);
            log.info("Notificación enviada a {} para mascota {}",
                    dueno.getEmail(), pet.getNombre());
            return true;

        } catch (Exception e) {
            log.error("Error al enviar notificación: {}", e.getMessage());
            return false;
        }
    }
}