package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.modelo.documentos.Notificacion;
import co.edu.uniquindio.proyecto.repositorios.NotificacionRepo;
import co.edu.uniquindio.proyecto.servicios.interfaces.NotificacionServicio;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class NotificacionServicioImpl implements NotificacionServicio {

    @Autowired
    private NotificacionRepo notificacionRepo;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    @PostConstruct
    public void inicializarFirebase() {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ClassPathResource(firebaseConfigPath).getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            System.err.println("Error al inicializar Firebase: " + e.getMessage());
        }
    }

    @Override
    public Notificacion crearNotificacion(Notificacion notificacion) {
        notificacion.setFecha(LocalDateTime.now());
        notificacion.setLeida(false);
        return notificacionRepo.save(notificacion);
    }

    @Override
    public void enviarNotificacionFirebase(ObjectId usuarioId, String titulo, String mensaje, ObjectId reporteId) {
        try {
            // Guardamos la notificación en la base de datos
            Notificacion notificacion = Notificacion.builder()
                    .mensaje(mensaje)
                    .fecha(LocalDateTime.now())
                    .tipo("FIREBASE")
                    .idUsuario(usuarioId)
                    .reporteId(reporteId)
                    .build();

            Notificacion notificacionGuardada = crearNotificacion(notificacion);

            // Preparamos los datos adicionales para la notificación
            Map<String, String> data = new HashMap<>();
            data.put("notificacionId", notificacionGuardada.getId().toString());
            data.put("reporteId", reporteId.toString());
            data.put("fecha", notificacionGuardada.getFecha().toString());

            // Enviamos la notificación a Firebase (asumiendo que tienes un token para el usuario)
            // En una implementación real, necesitarías almacenar y recuperar el token FCM para cada usuario
            String tokenFCM = obtenerTokenUsuario(usuarioId);

            if (tokenFCM != null && !tokenFCM.isEmpty()) {
                Message fcmMessage = Message.builder()
                        .setNotification(Notification.builder()
                                .setTitle(titulo)
                                .setBody(mensaje)
                                .build())
                        .putAllData(data)
                        .setToken(tokenFCM)
                        .build();

                FirebaseMessaging.getInstance().send(fcmMessage);
            }
        } catch (Exception e) {
            System.err.println("Error al enviar notificación Firebase: " + e.getMessage());
        }
    }

    // Método para obtener el token FCM del usuario (deberías implementar esto según tu modelo de datos)
    private String obtenerTokenUsuario(ObjectId usuarioId) {
        // Aquí deberías implementar la lógica para obtener el token FCM de tu base de datos
        // Por ejemplo, podrías tener un repositorio de usuarios con un campo para el token FCM
        return "token_fcm_del_usuario"; // Reemplazar con la lógica real
    }

    @Override
    public void enviarNotificacionEmail(String email, String asunto, String mensaje) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(remitente);
            mailMessage.setTo(email);
            mailMessage.setSubject(asunto);
            mailMessage.setText(mensaje);

            javaMailSender.send(mailMessage);
        } catch (Exception e) {
            System.err.println("Error al enviar email: " + e.getMessage());
        }
    }

    @Override
    public List<Notificacion> listarNotificacionesUsuario(ObjectId usuarioId) {
        return notificacionRepo.findByIdUsuarioOrderByFechaDesc(usuarioId);
    }

    @Override
    public Notificacion marcarNotificacionComoLeida(ObjectId notificacionId) {
        Optional<Notificacion> optionalNotificacion = notificacionRepo.findById(notificacionId);

        if (optionalNotificacion.isPresent()) {
            Notificacion notificacion = optionalNotificacion.get();
            notificacion.setLeida(true);
            return notificacionRepo.save(notificacion);
        }

        return null;
    }

    @Override
    public void eliminarNotificacion(ObjectId notificacionId) {
        notificacionRepo.deleteById(notificacionId);
    }

}