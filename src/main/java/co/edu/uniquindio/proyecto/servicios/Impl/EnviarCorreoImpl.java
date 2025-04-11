package co.edu.uniquindio.proyecto.servicios.Impl;

import co.edu.uniquindio.proyecto.dto.EnviarCorreoDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.EmailServicio;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnviarCorreoImpl implements EmailServicio {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Implementacion del servicio que envia un email usando JavaMailSender
     * a traves de un email establecido para enviar los correos
     * @param emailDTO esta compuesto por destino, asunto y cuerpo
     * @throws Exception
     */
    @Override
    public void enviarCorreo(EnviarCorreoDTO emailDTO) throws Exception {

        //enviar el codigo al email del parametro
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom("diazorli64@gmail.com"); // debe coincidir con el username en application.properties
        mensaje.setTo(emailDTO.destinatario());
        mensaje.setSubject(emailDTO.asunto());
        mensaje.setText(emailDTO.cuerpo());

        mailSender.send(mensaje);

    }
}