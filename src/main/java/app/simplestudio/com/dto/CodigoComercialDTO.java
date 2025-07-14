package app.simplestudio.com.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ==================== Código Comercial ====================
public record CodigoComercialDTO(
    @NotBlank(message = "Tipo de código comercial es requerido")
    String tipo,
    
    @NotBlank(message = "Código comercial es requerido")
    @Size(max = 20, message = "Código comercial no puede exceder 20 caracteres")
    String codigo
) {}