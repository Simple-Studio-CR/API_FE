package app.simplestudio.com.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

// ==================== Descuento ====================
public record DescuentoDTO(
    @NotNull(message = "Monto de descuento es requerido")
    @DecimalMin(value = "0.00", message = "Monto de descuento no puede ser negativo")
    BigDecimal montoDescuento,
    
    @NotBlank(message = "Naturaleza del descuento es requerida")
    @Size(max = 80, message = "Naturaleza del descuento no puede exceder 80 caracteres")
    String naturalezaDescuento
) {}