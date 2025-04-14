package co.edu.uniquindio.proyecto.config;

import co.edu.uniquindio.proyecto.seguridad.AutenticacionEntryPoint;
import co.edu.uniquindio.proyecto.seguridad.JWTFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> req
                        // Endpoints PÚBLICOS (sin autenticación)
                        .requestMatchers(
                                "/api/auth/**",  // ¡ESTA LÍNEA ES CLAVE! Incluye /api/auth/login
                                "/api/usuarios/{email}/verificarCodigoActivacionUsuario",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/v3/api-docs",
                                "/v3/api-docs/swagger-config",
                                "/swagger-ui/index.html"
                        ).permitAll()

                        // Registro de usuarios (público)
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()

                        // Endpoints de administración
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMINISTRADOR")

                        // Endpoints de clientes
                        .requestMatchers("/api/clientes/**").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMINISTRADOR")

                        // Endpoints de usuarios
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasAnyAuthority("ROLE_ADMINISTRADOR", "ROLE_CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/**").authenticated()

                        // Permisos para reportes
                        .requestMatchers(HttpMethod.POST, "/api/reportes").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/reportes/**").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/reportes/**").hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/reportes/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/reportes/**/rechazar").hasAnyAuthority("ROLE_ADMINISTRADOR")

                        // Todos los demás endpoints requieren autenticación
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(new AutenticacionEntryPoint()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization")); // Importante para JWT
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}