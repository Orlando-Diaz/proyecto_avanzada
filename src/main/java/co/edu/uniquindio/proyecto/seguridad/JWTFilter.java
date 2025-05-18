package co.edu.uniquindio.proyecto.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtils jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        //Obtener el token del header de la solicitud
        String token = getToken(request);

        //Si no hay token, continuar con la cadena de filtros
        if (token == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            //Validar el token y obtener el payload
            Jws<Claims> payload = jwtUtil.parseJwt(token);

            // Extraer información del token
            String userId = payload.getPayload().getSubject(); // Este es el ID de MongoDB
            String email = payload.getPayload().get("email", String.class); // Este es el email
            String role = payload.getPayload().get("rol", String.class);

            // Imprimir información para depuración
            System.out.println("JWT - UserID: " + userId);
            System.out.println("JWT - Email: " + email);
            System.out.println("JWT - Rol: " + role);

            // Usar el email como identificador principal en lugar del ID
            String username = email != null ? email : userId;

            //Si el usuario no está autenticado, crear un nuevo objeto de autenticación
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                //Crear un objeto UserDetails con el CORREO como nombre de usuario y el rol
                UserDetails userDetails = new User(
                        username,
                        "",
                        List.of(new SimpleGrantedAuthority(role))
                );

                //Crear un objeto de autenticación y establecerlo en el contexto de seguridad
                UsernamePasswordAuthenticationToken authentication = new
                        UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            // Imprimir el error para depuración
            System.err.println("Error al procesar JWT: " + e.getMessage());
            e.printStackTrace();

            //Si el token no es válido, enviar un error 401
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        //Continuar con la cadena de filtros
        chain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ") ? header.replace("Bearer ", "") :
                null;
    }
}