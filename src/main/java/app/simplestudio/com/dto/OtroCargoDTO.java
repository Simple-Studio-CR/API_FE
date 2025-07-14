package app.simplestudio.com.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

// ==================== Otro Cargo ====================
public record OtroCargoDTO(
    @NotBlank(message = "Tipo de documento es requerido")
    String tipoDocumento,
    
    String numeroIdentidadTercero,
    String nombreTercero,
    
    @NotBlank(message = "Detalle del cargo es requerido")
    @Size(max = 160, message = "Detalle no puede exceder 160 caracteres")
    String detalle,
    
    String porcentaje,
    
    @NotNull(message = "Monto del cargo es requerido")
    @DecimalMin(value = "0.01", message = "Monto del cargo debe ser mayor a 0")
    BigDecimal montoCargo
) {}