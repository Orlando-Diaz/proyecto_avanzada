package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.EnviarCorreoDTO;
import co.edu.uniquindio.proyecto.dto.NotificacionDTO;
import co.edu.uniquindio.proyecto.mapper.NotificacionMapper;
import co.edu.uniquindio.proyecto.modelo.documentos.Notificacion;
import co.edu.uniquindio.proyecto.repositorios.NotificacionRepo;
import co.edu.uniquindio.proyecto.servicios.interfaces.NotificacionServicio;


import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificacionServicioImpl implements NotificacionServicio {


    private final NotificacionRepo notificacionRepo;
    private final NotificacionMapper notificacionMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final JavaMailSender emailSender;

    @Override
    public NotificacionDTO crearNotificacion(NotificacionDTO notificacionDTO) {
        // Asegurar que la fecha es la actual si no viene establecida
        NotificacionDTO dtoConFecha = notificacionDTO.fecha() == null
                ? new NotificacionDTO(
                notificacionDTO.id(),
                notificacionDTO.mensaje(),
                LocalDateTime.now(),
                notificacionDTO.tipo(),
                notificacionDTO.leida(),
                notificacionDTO.reporteId(),
                notificacionDTO.idUsuario(),
                notificacionDTO.titulo())
                : notificacionDTO;

        // Guardar en la base de datos
        Notificacion notificacion = notificacionMapper.toEntity(dtoConFecha);
        Notificacion guardada = notificacionRepo.save(notificacion);

        NotificacionDTO dto = notificacionMapper.toDTO(guardada);

        // Enviar por WebSocket
        enviarNotificacionPorWebSocket(dto);

        return dto;
    }

    @Override
    public void enviarNotificacionPorWebSocket(NotificacionDTO notificacion) {
        // Enviamos al usuario específico
        messagingTemplate.convertAndSendToUser(
                notificacion.idUsuario(),
                "/queue/notifications",
                notificacion
        );

        // También podemos enviar a un topic general
        messagingTemplate.convertAndSend("/topic/notifications", notificacion);
    }


    @Override
    public List<NotificacionDTO> listarNotificacionesPorUsuario(String idUsuario) {
        List<Notificacion> notificaciones = notificacionRepo.findByIdUsuario(new ObjectId(idUsuario));
        return notificaciones.stream()
                .map(notificacionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void marcarNotificacionComoLeida(String idNotificacion) {
        Notificacion notificacion = notificacionRepo.findById(new ObjectId(idNotificacion))
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        notificacion.setLeida(true);
        notificacionRepo.save(notificacion);
    }

    @Override
    public List<NotificacionDTO> listarNotificacionesNoLeidas(String idUsuario) {
        List<Notificacion> notificaciones = notificacionRepo.findByIdUsuarioAndLeida(
                new ObjectId(idUsuario), false);

        return notificaciones.stream()
                .map(notificacionMapper::toDTO)
                .collect(Collectors.toList());
    }
}