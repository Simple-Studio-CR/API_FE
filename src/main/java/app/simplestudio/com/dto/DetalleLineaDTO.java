// ==================== DetalleLineaDTO con Builder ====================
package app.simplestudio.com.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para líneas de detalle de documentos electrónicos
 */
public record DetalleLineaDTO(

    @NotNull(message = "Número de línea es requerido")
    @Min(value = 1, message = "Número de línea debe ser mayor a 0")
    Integer numeroLinea,

    String partidaArancelaria,

    String codigo,

    @Valid
    List<CodigoComercialDTO> codigoComercial,

    @NotNull(message = "Cantidad es requerida")
    @DecimalMin(value = "0.01", message = "Cantidad debe ser mayor a 0")
    BigDecimal cantidad,

    @NotBlank(message = "Unidad de medida es requerida")
    String unidadMedida,

    String unidadMedidaComercial,

    @NotBlank(message = "Detalle es requerido")
    @Size(max = 200, message = "Detalle no puede exceder 200 caracteres")
    String detalle,

    @NotNull(message = "Precio unitario es requerido")
    @DecimalMin(value = "0.01", message = "Precio unitario debe ser mayor a 0")
    BigDecimal precioUnitario,

    @NotNull(message = "Monto total es requerido")
    BigDecimal montoTotal,

    @Valid
    List<DescuentoDTO> descuentos,

    BigDecimal subTotal,

    @Valid
    List<ImpuestoItemDTO> impuestos,

    BigDecimal impuestoNeto,

    @NotNull(message = "Monto total línea es requerido")
    BigDecimal montoTotalLinea,

    @Valid
    List<ExoneracionDTO> exoneraciones
) {

    // ==================== Builder Pattern ====================
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer numeroLinea;
        private String partidaArancelaria;
        private String codigo;
        private List<CodigoComercialDTO> codigoComercial;
        private BigDecimal cantidad;
        private String unidadMedida;
        private String unidadMedidaComercial;
        private String detalle;
        private BigDecimal precioUnitario;
        private BigDecimal montoTotal;
        private List<DescuentoDTO> descuentos;
        private BigDecimal subTotal;
        private List<ImpuestoItemDTO> impuestos;
        private BigDecimal impuestoNeto;
        private BigDecimal montoTotalLinea;
        private List<ExoneracionDTO> exoneraciones;

        public Builder numeroLinea(Integer numeroLinea) {
            this.numeroLinea = numeroLinea;
            return this;
        }

        public Builder partidaArancelaria(String partidaArancelaria) {
            this.partidaArancelaria = partidaArancelaria;
            return this;
        }

        public Builder codigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public Builder codigoComercial(List<CodigoComercialDTO> codigoComercial) {
            this.codigoComercial = codigoComercial;
            return this;
        }

        public Builder cantidad(BigDecimal cantidad) {
            this.cantidad = cantidad;
            return this;
        }

        public Builder unidadMedida(String unidadMedida) {
            this.unidadMedida = unidadMedida;
            return this;
        }

        public Builder unidadMedidaComercial(String unidadMedidaComercial) {
            this.unidadMedidaComercial = unidadMedidaComercial;
            return this;
        }

        public Builder detalle(String detalle) {
            this.detalle = detalle;
            return this;
        }

        public Builder precioUnitario(BigDecimal precioUnitario) {
            this.precioUnitario = precioUnitario;
            return this;
        }

        public Builder montoTotal(BigDecimal montoTotal) {
            this.montoTotal = montoTotal;
            return this;
        }

        public Builder descuentos(List<DescuentoDTO> descuentos) {
            this.descuentos = descuentos;
            return this;
        }

        public Builder subTotal(BigDecimal subTotal) {
            this.subTotal = subTotal;
            return this;
        }

        public Builder impuestos(List<ImpuestoItemDTO> impuestos) {
            this.impuestos = impuestos;
            return this;
        }

        public Builder impuestoNeto(BigDecimal impuestoNeto) {
            this.impuestoNeto = impuestoNeto;
            return this;
        }

        public Builder montoTotalLinea(BigDecimal montoTotalLinea) {
            this.montoTotalLinea = montoTotalLinea;
            return this;
        }

        public Builder exoneraciones(List<ExoneracionDTO> exoneraciones) {
            this.exoneraciones = exoneraciones;
            return this;
        }

        public DetalleLineaDTO build() {
            return new DetalleLineaDTO(
                numeroLinea, partidaArancelaria, codigo, codigoComercial,
                cantidad, unidadMedida, unidadMedidaComercial, detalle,
                precioUnitario, montoTotal, descuentos, subTotal,
                impuestos, impuestoNeto, montoTotalLinea, exoneraciones
            );
        }
    }
}