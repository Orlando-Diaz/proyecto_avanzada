package co.edu.uniquindio.proyecto.servicios.interfaces;

import co.edu.uniquindio.proyecto.dto.EnviarCorreoDTO;

public interface EmailServicio {

    void enviarCorreo(EnviarCorreoDTO emailDTO) throws Exception;

}
