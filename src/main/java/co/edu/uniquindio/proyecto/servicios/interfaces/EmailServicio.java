package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.EnviarCorreoDTO;

public interface EmailServicio {

    void enviarCorreo(EnviarCorreoDTO emailDTO) throws Exception;


    /* EJEMPLO DE METOD PARA ENVIAR CORREO ELECTRONICO

    emailServicio.enviarCorreo( new EmailDTO("Asunto", "Cuerpo mensaje", "Correo destino") );

     */
}
