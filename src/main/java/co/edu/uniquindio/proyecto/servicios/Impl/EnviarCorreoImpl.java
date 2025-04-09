package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.EnviarCorreoDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.EmailServicio;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnviarCorreoImpl implements EmailServicio {


}