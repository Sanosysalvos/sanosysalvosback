package com.sanosysalvos.ms_notification.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    // Añadimos @Async si decidiste usarlo para que no bloquee el front
    public void enviarCorreo(String destinatario, String asunto, String cuerpoMensaje) {
        Email from = new Email(fromEmail);
        Email to = new Email(destinatario);
        
        // El cuerpoMensaje aquí ya vendrá con el texto del usuario concatenado desde el Controller
        Content content = new Content("text/plain", cuerpoMensaje);
        Mail mail = new Mail(from, asunto, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            
            // Log clave para mañana: si ves 202, todo salió bien.
            System.out.println("SendGrid Response Status: " + response.getStatusCode());
            
            if (response.getStatusCode() >= 400) {
                System.err.println("Error de SendGrid: " + response.getBody());
            }
            
        } catch (IOException ex) {
            System.err.println("Error de conexión con SendGrid: " + ex.getMessage());
        }
    }
}