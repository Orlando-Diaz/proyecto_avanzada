// Actualización completa del SecurityConfig con endpoints de notificaciones

package co.edu.uniquindio.proyecto.seguridad;

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
                                "/api/auth/**",
                                "/api/usuarios/{email}/verificarCodigoActivacionUsuario",
                                "/api/usuarios/recuperarContrasenia",
                                "/api/usuarios/cambiarContrasenia",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/v3/api-docs",
                                "/v3/api-docs/swagger-config",
                                "/swagger-ui/index.html",
                                "/ws/**" // WebSocket endpoints
                        ).permitAll()

                        // Registro de usuarios (público)
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()

                        // CONFIGURACIÓN PARA CATEGORÍAS
                        .requestMatchers(HttpMethod.GET, "/api/categorias").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/categorias").hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/{id}").hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/{id}").hasAuthority("ROLE_ADMINISTRADOR")

                        // Endpoints de Notificaciones
                        .requestMatchers(HttpMethod.GET, "/api/notificaciones").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/notificaciones/no-leidas").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/notificaciones/{id}/leer").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/notificaciones").hasAuthority("ROLE_ADMINISTRADOR")

                        // WebSocket
                        .requestMatchers("/ws/**").permitAll()

                        // Endpoints de administración
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMINISTRADOR")

                        // Endpoints de clientes
                        .requestMatchers("/api/clientes/**").hasAuthority("ROLE_CLIENTE")

                        // Endpoints específicos del cliente para su perfil
                        .requestMatchers(HttpMethod.GET, "/api/clientes/perfil").hasAuthority("ROLE_CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/perfil").hasAuthority("ROLE_CLIENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/cuenta").hasAuthority("ROLE_CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/cambiar-password").hasAuthority("ROLE_CLIENTE")

                        // Endpoints de usuarios (administración)
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/{id}").hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/{id}").hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/{id}").hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").hasAuthority("ROLE_ADMINISTRADOR")

                        // Permisos para reportes
                        .requestMatchers(HttpMethod.POST, "/api/reportes").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/reportes/{id}").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/reportes/{id}").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/reportes").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/reportes/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/reportes/mis-reportes").hasAuthority("ROLE_CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/reportes/{id}/rechazar").hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/reportes/gestionar-estado-reporte-admin").hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/reportes/cliente/gestionar-estado-reporte-usuario").hasAuthority("ROLE_CLIENTE")

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
        config.setExposedHeaders(List.of("Authorization"));
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