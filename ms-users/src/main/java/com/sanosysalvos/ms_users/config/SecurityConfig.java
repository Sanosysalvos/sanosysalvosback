package com.sanosysalvos.ms_users.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Vincula la configuración de CORS que definimos abajo
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
            
            // 2. Deshabilita CSRF porque usamos tokens (stateless)
            .csrf(csrf -> csrf.disable())    
            
            // 3. Reglas de autorización (Quién entra y quién no)
            .authorizeHttpRequests(auth -> auth
                // Endpoints PÚBLICOS: Permitimos revisar si el email o rut existen sin estar logueado
                .requestMatchers(HttpMethod.GET, "/api/users/check-email", "/api/users/check-rut").permitAll()
                
                // Endpoints PRIVADOS: Cualquier otra acción en usuarios requiere token válido
                .requestMatchers("/api/users/**").authenticated()
                
                // Cerrrojo total: Cualquier otra petición que no coincida arriba, también se bloquea
                .anyRequest().authenticated()
            )
            
            // 4. El guardián: Valida automáticamente los JWT (Tokens) de Firebase
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Mantenemos tus dos orígenes clave para que el navegador no bloquee Vercel ni Localhost
        configuration.setAllowedOrigins(Arrays.asList(
            "https://sanosysalvos-five.vercel.app", 
            "http://localhost:3000"
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // IMPORTANTE: Al activar seguridad, el Frontend DEBE tener permiso de enviar el Header "Authorization"
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}