package com.nextbank.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * TODO(SEGURIDAD): configuración PROVISIONAL de desarrollo local.
 *
 * Deja TODOS los endpoints abiertos y desactiva CSRF para poder probar la API
 * sin Keycloak levantado. NO debe llegar a ningún entorno desplegado.
 *
 * Sustituir por validación de JWT contra Keycloak (ver ADR-0005) en cuanto
 * el realm 'nextbank' esté disponible:
 *
 *   http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
 *   http.authorizeHttpRequests(auth -> auth
 *           .requestMatchers("/actuator/health").permitAll()
 *           .anyRequest().authenticated());
 *
 * El @Profile("local") es la red de seguridad: esta clase solo se activa con
 * el perfil 'local', así que aunque se olvide borrar, no aplica en otros entornos.
 */
@Configuration
@Profile("local")
public class SecurityConfig {

    @Bean
    SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
