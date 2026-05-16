package com.sanosysalvos.bff.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

// Excluimos la configuración automática de seguridad básica
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class BffApplication {
    public static void main(String[] args) {
        SpringApplication.run(BffApplication.class, args);
    }
}