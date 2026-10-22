package com.DisenoProductos.EcoSolido.Security;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    // Frontends adicionales permitidos (p. ej. el despliegue en Vercel), sin tocar código
    @Value("${cors.origenes-extra:}")
    private String origenesExtra;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(
                                "/usuario/registrar",
                                "/usuario/autenticar",
                                "/usuario/verificar-cor-o-tel",
                                "/usuario/pregunta-seguridad",
                                "/usuario/verificar-respuesta",
                                "/usuario/restablecer-contrasena"
                        ).permitAll()
                        // Gestión administrativa: solo rol ADMIN
                        .requestMatchers(HttpMethod.GET, "/usuario/listar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/usuario/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/usuario/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/incidencias/mostrarT").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/incidencias/usuario/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/incidencias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/incidencias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/incidencias/cambiarEstado/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // ← agrega este método
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Orígenes fijos + los que se agreguen en la variable CORS_ORIGENES_EXTRA
        // (separados por comas; admite comodines, p. ej. https://mi-app-*.vercel.app)
        List<String> origenes = new ArrayList<>(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "https://ecosolido-frontend.onrender.com",
                "https://frontend-trabajofinal.onrender.com"
        ));
        Arrays.stream(origenesExtra.split(","))
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .map(o -> o.endsWith("/") ? o.substring(0, o.length() - 1) : o)
                .forEach(origenes::add);
        config.setAllowedOriginPatterns(origenes);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

