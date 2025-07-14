package app.simplestudio.com.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

// ==================== Impuesto de Ítem ====================
public record ImpuestoItemDTO(
    @NotBlank(message = "Código de impuesto es requerido")
    String codigo,
    
    @NotNull(message = "Tarifa es requerida")
    @DecimalMin(value = "0.00", message = "Tarifa no puede ser negativa")
    BigDecimal tarifa,
    
    @NotBlank(message = "Código de tarifa es requerido")
    String codigoTarifa,
    
    @NotNull(message = "Monto es requerido")
    @DecimalMin(value = "0.00", message = "Monto no puede ser negativo")
    BigDecimal monto,
    
    BigDecimal impuestoNeto,
    
    // Exoneración (opcional)
    ExoneracionDTO exoneracion
) {}