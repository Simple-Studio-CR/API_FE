package app.simplestudio.com.service.mapper;

import app.simplestudio.com.dto.FacturaRequestDTO;

public interface JsonToFacturaMapper {
    
    /**
     * Convierte JSON String a FacturaRequestDTO
     * @param jsonString JSON de entrada del controlador
     * @return DTO tipado y validado
     */
    FacturaRequestDTO mapFromJson(String jsonString);
}