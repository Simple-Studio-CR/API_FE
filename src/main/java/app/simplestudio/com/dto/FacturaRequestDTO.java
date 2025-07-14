// ==================== FacturaRequestDTO con Builder ====================
package app.simplestudio.com.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO principal para recepción de documentos electrónicos
 * Maneja: FE, TE, NC, ND, FEC, FEE
 */
public record FacturaRequestDTO(

    // ========== Información Básica ==========
    @NotBlank(message = "Tipo de documento es requerido")
    String tipoDocumento,

    @NotBlank(message = "Situación es requerida")
    String situacion,

    @NotNull(message = "Sucursal es requerida")
    Integer sucursal,

    @NotNull(message = "Terminal es requerida")
    Integer terminal,

    @NotBlank(message = "Emisor es requerido")
    String emisor,

    @NotBlank(message = "Token de acceso es requerido")
    String tokenAccess,

    // Campos opcionales
    String clave,
    String fechaEmision,
    String fechaEmisionCliente,
    String numeroFactura,
    String codigoActividadEmisor,

    // ========== Información del Receptor ==========
    String omitirReceptor,
    String receptorNombre,
    String receptorTipoIdentif,
    String receptorNumIdentif,
    String receptorProvincia,
    String receptorCanton,
    String receptorDistrito,
    String receptorBarrio,
    String receptorOtrasSenas,
    String receptorCodPaisTel,
    String receptorTel,
    String receptorCodPaisFax,
    String receptorFax,
    String receptorEmail,

    // ========== Información Monetaria ==========
    String codMoneda,
    BigDecimal tipoCambio,
    String condVenta,
    Integer plazoCredito,

    // ========== Medios de Pago ==========
    String medioPago,
    String medioPago2,
    String medioPago3,
    String medioPago4,

    // ========== Totales ==========
    BigDecimal totalServGravados,
    BigDecimal totalServExentos,
    BigDecimal totalServExonerado,
    BigDecimal totalMercGravadas,
    BigDecimal totalMercExentas,
    BigDecimal totalMercExonerada,
    BigDecimal totalGravados,
    BigDecimal totalExentos,
    BigDecimal totalExonerado,
    BigDecimal totalVentas,
    BigDecimal totalDescuentos,
    BigDecimal totalVentasNeta,
    BigDecimal totalImp,
    BigDecimal totalIVADevuelto,
    BigDecimal totalOtrosCargos,
    BigDecimal totalComprobante,
    String otros,

    // ========== Detalles y Referencias ==========
    @Valid
    List<DetalleLineaDTO> detalleLinea,

    @Valid
    List<ReferenciaDTO> referencias,

    @Valid
    List<OtroCargoDTO> otrosCargos,

    // ========== Campos específicos para Mensajes Receptor ==========
    String mensaje,
    String detalleMensaje,
    String condicionImpuesto,
    BigDecimal montoTotalImpuestoAcreditar,
    BigDecimal montoTotalDeGastoAplicable,
    BigDecimal montoTotalImpuesto,
    BigDecimal totalFactura,
    String claveDocumentoEmisor
) {

    // ==================== Builder Pattern ====================
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String tipoDocumento;
        private String situacion;
        private Integer sucursal;
        private Integer terminal;
        private String emisor;
        private String tokenAccess;
        private String clave;
        private String fechaEmision;
        private String fechaEmisionCliente;
        private String numeroFactura;
        private String codigoActividadEmisor;

        // Receptor
        private String omitirReceptor;
        private String receptorNombre;
        private String receptorTipoIdentif;
        private String receptorNumIdentif;
        private String receptorProvincia;
        private String receptorCanton;
        private String receptorDistrito;
        private String receptorBarrio;
        private String receptorOtrasSenas;
        private String receptorCodPaisTel;
        private String receptorTel;
        private String receptorCodPaisFax;
        private String receptorFax;
        private String receptorEmail;

        // Monetaria
        private String codMoneda;
        private BigDecimal tipoCambio;
        private String condVenta;
        private Integer plazoCredito;

        // Medios de pago
        private String medioPago;
        private String medioPago2;
        private String medioPago3;
        private String medioPago4;

        // Totales
        private BigDecimal totalServGravados;
        private BigDecimal totalServExentos;
        private BigDecimal totalServExonerado;
        private BigDecimal totalMercGravadas;
        private BigDecimal totalMercExentas;
        private BigDecimal totalMercExonerada;
        private BigDecimal totalGravados;
        private BigDecimal totalExentos;
        private BigDecimal totalExonerado;
        private BigDecimal totalVentas;
        private BigDecimal totalDescuentos;
        private BigDecimal totalVentasNeta;
        private BigDecimal totalImp;
        private BigDecimal totalIVADevuelto;
        private BigDecimal totalOtrosCargos;
        private BigDecimal totalComprobante;
        private String otros;

        // Listas
        private List<DetalleLineaDTO> detalleLinea;
        private List<ReferenciaDTO> referencias;
        private List<OtroCargoDTO> otrosCargos;

        // Mensaje receptor
        private String mensaje;
        private String detalleMensaje;
        private String condicionImpuesto;
        private BigDecimal montoTotalImpuestoAcreditar;
        private BigDecimal montoTotalDeGastoAplicable;
        private BigDecimal montoTotalImpuesto;
        private BigDecimal totalFactura;
        private String claveDocumentoEmisor;

        // Métodos del builder
        public Builder tipoDocumento(String tipoDocumento) {
            this.tipoDocumento = tipoDocumento;
            return this;
        }

        public Builder situacion(String situacion) {
            this.situacion = situacion;
            return this;
        }

        public Builder sucursal(Integer sucursal) {
            this.sucursal = sucursal;
            return this;
        }

        public Builder terminal(Integer terminal) {
            this.terminal = terminal;
            return this;
        }

        public Builder emisor(String emisor) {
            this.emisor = emisor;
            return this;
        }

        public Builder tokenAccess(String tokenAccess) {
            this.tokenAccess = tokenAccess;
            return this;
        }

        public Builder clave(String clave) {
            this.clave = clave;
            return this;
        }

        public Builder fechaEmision(String fechaEmision) {
            this.fechaEmision = fechaEmision;
            return this;
        }

        public Builder fechaEmisionCliente(String fechaEmisionCliente) {
            this.fechaEmisionCliente = fechaEmisionCliente;
            return this;
        }

        public Builder numeroFactura(String numeroFactura) {
            this.numeroFactura = numeroFactura;
            return this;
        }

        public Builder codigoActividadEmisor(String codigoActividadEmisor) {
            this.codigoActividadEmisor = codigoActividadEmisor;
            return this;
        }

        // Receptor methods
        public Builder omitirReceptor(String omitirReceptor) {
            this.omitirReceptor = omitirReceptor;
            return this;
        }

        public Builder receptorNombre(String receptorNombre) {
            this.receptorNombre = receptorNombre;
            return this;
        }

        public Builder receptorTipoIdentif(String receptorTipoIdentif) {
            this.receptorTipoIdentif = receptorTipoIdentif;
            return this;
        }

        public Builder receptorNumIdentif(String receptorNumIdentif) {
            this.receptorNumIdentif = receptorNumIdentif;
            return this;
        }

        public Builder receptorProvincia(String receptorProvincia) {
            this.receptorProvincia = receptorProvincia;
            return this;
        }

        public Builder receptorCanton(String receptorCanton) {
            this.receptorCanton = receptorCanton;
            return this;
        }

        public Builder receptorDistrito(String receptorDistrito) {
            this.receptorDistrito = receptorDistrito;
            return this;
        }

        public Builder receptorBarrio(String receptorBarrio) {
            this.receptorBarrio = receptorBarrio;
            return this;
        }

        public Builder receptorOtrasSenas(String receptorOtrasSenas) {
            this.receptorOtrasSenas = receptorOtrasSenas;
            return this;
        }

        public Builder receptorCodPaisTel(String receptorCodPaisTel) {
            this.receptorCodPaisTel = receptorCodPaisTel;
            return this;
        }

        public Builder receptorTel(String receptorTel) {
            this.receptorTel = receptorTel;
            return this;
        }

        public Builder receptorCodPaisFax(String receptorCodPaisFax) {
            this.receptorCodPaisFax = receptorCodPaisFax;
            return this;
        }

        public Builder receptorFax(String receptorFax) {
            this.receptorFax = receptorFax;
            return this;
        }

        public Builder receptorEmail(String receptorEmail) {
            this.receptorEmail = receptorEmail;
            return this;
        }

        // Monetary methods
        public Builder codMoneda(String codMoneda) {
            this.codMoneda = codMoneda;
            return this;
        }

        public Builder tipoCambio(BigDecimal tipoCambio) {
            this.tipoCambio = tipoCambio;
            return this;
        }

        public Builder condVenta(String condVenta) {
            this.condVenta = condVenta;
            return this;
        }

        public Builder plazoCredito(Integer plazoCredito) {
            this.plazoCredito = plazoCredito;
            return this;
        }

        // Payment methods
        public Builder medioPago(String medioPago) {
            this.medioPago = medioPago;
            return this;
        }

        public Builder medioPago2(String medioPago2) {
            this.medioPago2 = medioPago2;
            return this;
        }

        public Builder medioPago3(String medioPago3) {
            this.medioPago3 = medioPago3;
            return this;
        }

        public Builder medioPago4(String medioPago4) {
            this.medioPago4 = medioPago4;
            return this;
        }

        // Totals methods
        public Builder totalServGravados(BigDecimal totalServGravados) {
            this.totalServGravados = totalServGravados;
            return this;
        }

        public Builder totalServExentos(BigDecimal totalServExentos) {
            this.totalServExentos = totalServExentos;
            return this;
        }

        public Builder totalServExonerado(BigDecimal totalServExonerado) {
            this.totalServExonerado = totalServExonerado;
            return this;
        }

        public Builder totalMercGravadas(BigDecimal totalMercGravadas) {
            this.totalMercGravadas = totalMercGravadas;
            return this;
        }

        public Builder totalMercExentas(BigDecimal totalMercExentas) {
            this.totalMercExentas = totalMercExentas;
            return this;
        }

        public Builder totalMercExonerada(BigDecimal totalMercExonerada) {
            this.totalMercExonerada = totalMercExonerada;
            return this;
        }

        public Builder totalGravados(BigDecimal totalGravados) {
            this.totalGravados = totalGravados;
            return this;
        }

        public Builder totalExentos(BigDecimal totalExentos) {
            this.totalExentos = totalExentos;
            return this;
        }

        public Builder totalExonerado(BigDecimal totalExonerado) {
            this.totalExonerado = totalExonerado;
            return this;
        }

        public Builder totalVentas(BigDecimal totalVentas) {
            this.totalVentas = totalVentas;
            return this;
        }

        public Builder totalDescuentos(BigDecimal totalDescuentos) {
            this.totalDescuentos = totalDescuentos;
            return this;
        }

        public Builder totalVentasNeta(BigDecimal totalVentasNeta) {
            this.totalVentasNeta = totalVentasNeta;
            return this;
        }

        public Builder totalImp(BigDecimal totalImp) {
            this.totalImp = totalImp;
            return this;
        }

        public Builder totalIVADevuelto(BigDecimal totalIVADevuelto) {
            this.totalIVADevuelto = totalIVADevuelto;
            return this;
        }

        public Builder totalOtrosCargos(BigDecimal totalOtrosCargos) {
            this.totalOtrosCargos = totalOtrosCargos;
            return this;
        }

        public Builder totalComprobante(BigDecimal totalComprobante) {
            this.totalComprobante = totalComprobante;
            return this;
        }

        public Builder otros(String otros) {
            this.otros = otros;
            return this;
        }

        // Lists methods
        public Builder detalleLinea(List<DetalleLineaDTO> detalleLinea) {
            this.detalleLinea = detalleLinea;
            return this;
        }

        public Builder referencias(List<ReferenciaDTO> referencias) {
            this.referencias = referencias;
            return this;
        }

        public Builder otrosCargos(List<OtroCargoDTO> otrosCargos) {
            this.otrosCargos = otrosCargos;
            return this;
        }

        // Mensaje receptor methods
        public Builder mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public Builder detalleMensaje(String detalleMensaje) {
            this.detalleMensaje = detalleMensaje;
            return this;
        }

        public Builder condicionImpuesto(String condicionImpuesto) {
            this.condicionImpuesto = condicionImpuesto;
            return this;
        }

        public Builder montoTotalImpuestoAcreditar(BigDecimal montoTotalImpuestoAcreditar) {
            this.montoTotalImpuestoAcreditar = montoTotalImpuestoAcreditar;
            return this;
        }

        public Builder montoTotalDeGastoAplicable(BigDecimal montoTotalDeGastoAplicable) {
            this.montoTotalDeGastoAplicable = montoTotalDeGastoAplicable;
            return this;
        }

        public Builder montoTotalImpuesto(BigDecimal montoTotalImpuesto) {
            this.montoTotalImpuesto = montoTotalImpuesto;
            return this;
        }

        public Builder totalFactura(BigDecimal totalFactura) {
            this.totalFactura = totalFactura;
            return this;
        }

        public Builder claveDocumentoEmisor(String claveDocumentoEmisor) {
            this.claveDocumentoEmisor = claveDocumentoEmisor;
            return this;
        }

        public FacturaRequestDTO build() {
            return new FacturaRequestDTO(
                tipoDocumento, situacion, sucursal, terminal, emisor, tokenAccess,
                clave, fechaEmision, fechaEmisionCliente, numeroFactura, codigoActividadEmisor,
                omitirReceptor, receptorNombre, receptorTipoIdentif, receptorNumIdentif,
                receptorProvincia, receptorCanton, receptorDistrito, receptorBarrio, receptorOtrasSenas,
                receptorCodPaisTel, receptorTel, receptorCodPaisFax, receptorFax, receptorEmail,
                codMoneda, tipoCambio, condVenta, plazoCredito,
                medioPago, medioPago2, medioPago3, medioPago4,
                totalServGravados, totalServExentos, totalServExonerado,
                totalMercGravadas, totalMercExentas, totalMercExonerada,
                totalGravados, totalExentos, totalExonerado,
                totalVentas, totalDescuentos, totalVentasNeta,
                totalImp, totalIVADevuelto, totalOtrosCargos, totalComprobante, otros,
                detalleLinea, referencias, otrosCargos,
                mensaje, detalleMensaje, condicionImpuesto,
                montoTotalImpuestoAcreditar, montoTotalDeGastoAplicable,
                montoTotalImpuesto, totalFactura, claveDocumentoEmisor
            );
        }
    }
}