package com.sanosysalvos.ms_notification.controller;

import com.sanosysalvos.ms_notification.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications") // Es buena práctica definir la ruta base aquí
public class NotificationController { // <-- Abre la clase

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-test")
    public String enviarPrueba(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String mascota = body.get("mascota");
        // Capturamos el mensaje dinámico del frontend
        String mensajeUsuario = body.get("mensaje"); 
        
        // Armamos un cuerpo descriptivo
        String cuerpoFinal = "¡Hola! Tenemos noticias de " + mascota + ".\n\n" +
                             "Un usuario te ha enviado el siguiente mensaje:\n" +
                             "\"" + mensajeUsuario + "\"\n\n" +
                             "¡Revisa la app para más detalles!";
        
        emailService.enviarCorreo(email, "¡Noticias de " + mascota + "!", cuerpoFinal);
        
        return "Correo enviado a " + email;
    }

} // <-- Esta es la llave final que te faltaba para cerrar la clase