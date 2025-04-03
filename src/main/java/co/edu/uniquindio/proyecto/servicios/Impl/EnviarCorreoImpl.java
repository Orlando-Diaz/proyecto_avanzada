package co.edu.uniquindio.proyecto.servicios.Impl;


import co.edu.uniquindio.proyecto.dto.EnviarCorreoDTO;
import co.edu.uniquindio.proyecto.servicios.interfaces.EmailServicio;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static org.simplejavamail.config.ConfigLoader.Property.SMTP_PORT;

@Service
public class EnviarCorreoImpl implements EmailServicio {


    @Override
    @Async
    public void enviarCorreo(EnviarCorreoDTO emailDTO) throws Exception {
        Email email = EmailBuilder.startingBlank()
                .from("SMTP_USERNAME")
                .to(emailDTO.destinatario())
                .withSubject(emailDTO.asunto())
                .withPlainText(emailDTO.cuerpo())
                .buildEmail();

        try (Mailer mailer = MailerBuilder
                .withSMTPServer("SMTP_HOST", 587, "SMTP_USERNAME", "SMTP_PASSWORD")
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withDebugLogging(true)
                .buildMailer()) {

            mailer.sendMail(email);
        }
    }
}
