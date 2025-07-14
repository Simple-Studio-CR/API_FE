package app.simplestudio.com.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

// ==================== Exoneración ====================
public record ExoneracionDTO(
    @NotBlank(message = "Tipo de documento de exoneración es requerido")
    String tipoDocumento,
    
    @NotBlank(message = "Número de documento de exoneración es requerido")
    @Size(max = 40, message = "Número de documento no puede exceder 40 caracteres")
    String numeroDocumento,
    
    @NotBlank(message = "Nombre de institución es requerido")
    @Size(max = 160, message = "Nombre de institución no puede exceder 160 caracteres")
    String nombreInstitucion,
    
    @NotBlank(message = "Fecha de emisión es requerida")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$",
             message = "Formato de fecha inválido")
    String fechaEmision,
    
    @NotNull(message = "Monto de exoneración es requerido")
    @DecimalMin(value = "0.00", message = "Monto de exoneración no puede ser negativo")
    BigDecimal montoExoneracion,
    
    @NotNull(message = "Porcentaje de exoneración es requerido")
    @Min(value = 0, message = "Porcentaje de exoneración no puede ser negativo")
    @Max(value = 100, message = "Porcentaje de exoneración no puede ser mayor a 100")
    Integer porcentajeExoneracion
) {}