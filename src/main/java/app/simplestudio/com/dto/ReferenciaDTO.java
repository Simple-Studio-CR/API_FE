package app.simplestudio.com.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// ==================== Referencia ====================
public record ReferenciaDTO(
    @NotBlank(message = "Número de referencia es requerido")
    @Size(min = 50, max = 50, message = "Número de referencia debe tener exactamente 50 caracteres")
    String numero,
    
    @NotBlank(message = "Fecha de emisión de referencia es requerida")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$",
             message = "Formato de fecha inválido")
    String fechaEmision,
    
    @NotBlank(message = "Código de referencia es requerido")
    String codigo,
    
    @NotBlank(message = "Razón de referencia es requerida")
    @Size(max = 180, message = "Razón no puede exceder 180 caracteres")
    String razon
) {}