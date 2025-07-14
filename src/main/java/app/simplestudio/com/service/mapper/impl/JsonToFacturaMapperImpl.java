// ==================== JsonToFacturaMapperImpl Corregido ====================
package app.simplestudio.com.service.mapper.impl;

import app.simplestudio.com.dto.*;
import app.simplestudio.com.service.mapper.JsonToFacturaMapper;
import app.simplestudio.com.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JsonToFacturaMapperImpl implements JsonToFacturaMapper {

    private static final Logger log = LoggerFactory.getLogger(JsonToFacturaMapperImpl.class);
    private final ObjectMapper objectMapper;

    public JsonToFacturaMapperImpl() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public FacturaRequestDTO mapFromJson(String jsonString) {
        try {
            log.debug("Mapeando JSON a FacturaRequestDTO");

            JsonNode jsonNode = objectMapper.readTree(jsonString);

            FacturaRequestDTO.Builder builder = FacturaRequestDTO.builder()
                // Información básica
                .tipoDocumento(getTextValue(jsonNode, "tipoDocumento"))
                .situacion(getTextValue(jsonNode, "situacion"))
                .sucursal(getIntValue(jsonNode, "sucursal"))
                .terminal(getIntValue(jsonNode, "terminal"))
                .emisor(getTextValue(jsonNode, "emisor"))
                .tokenAccess(getTextValue(jsonNode, "tokenAccess"))
                .clave(getTextValue(jsonNode, "clave"))
                .fechaEmision(getTextValue(jsonNode, "fechaEmision"))
                .fechaEmisionCliente(getTextValue(jsonNode, "fechaEmisionCliente"))
                .numeroFactura(getTextValue(jsonNode, "numeroFactura"))
                .codigoActividadEmisor(getTextValue(jsonNode, "codigoActividad"))

                // Receptor
                .omitirReceptor(getTextValue(jsonNode, "omitirReceptor"))
                .receptorNombre(getTextValue(jsonNode, "receptorNombre"))
                .receptorTipoIdentif(getTextValue(jsonNode, "receptorTipoIdentif"))
                .receptorNumIdentif(getTextValue(jsonNode, "receptorNumIdentif"))
                .receptorProvincia(getTextValue(jsonNode, "receptorProvincia"))
                .receptorCanton(getTextValue(jsonNode, "receptorCanton"))
                .receptorDistrito(getTextValue(jsonNode, "receptorDistrito"))
                .receptorBarrio(getTextValue(jsonNode, "receptorBarrio"))
                .receptorOtrasSenas(getTextValue(jsonNode, "receptorOtrasSenas"))
                .receptorCodPaisTel(getTextValue(jsonNode, "receptorCodPaisTel"))
                .receptorTel(getTextValue(jsonNode, "receptorTel"))
                .receptorCodPaisFax(getTextValue(jsonNode, "receptorCodPaisFax"))
                .receptorFax(getTextValue(jsonNode, "receptorFax"))
                .receptorEmail(getTextValue(jsonNode, "receptorEmail"))

                // Información monetaria
                .codMoneda(getTextValueWithDefault(jsonNode, "codMoneda", "CRC"))
                .tipoCambio(getBigDecimalValueWithDefault(jsonNode, "tipoCambio", "1"))
                .condVenta(getTextValue(jsonNode, "condVenta"))
                .plazoCredito(getIntValue(jsonNode, "plazoCredito"))

                // Medios de pago
                .medioPago(getTextValue(jsonNode, "medioPago"))
                .medioPago2(getTextValue(jsonNode, "medioPago2"))
                .medioPago3(getTextValue(jsonNode, "medioPago3"))
                .medioPago4(getTextValue(jsonNode, "medioPago4"))

                // Totales
                .totalServGravados(getBigDecimalValue(jsonNode, "totalServGravados"))
                .totalServExentos(getBigDecimalValue(jsonNode, "totalServExentos"))
                .totalServExonerado(getBigDecimalValue(jsonNode, "totalServExonerado"))
                .totalMercGravadas(getBigDecimalValue(jsonNode, "totalMercGravadas"))
                .totalMercExentas(getBigDecimalValue(jsonNode, "totalMercExentas"))
                .totalMercExonerada(getBigDecimalValue(jsonNode, "totalMercExonerada"))
                .totalGravados(getBigDecimalValue(jsonNode, "totalGravados"))
                .totalExentos(getBigDecimalValue(jsonNode, "totalExentos"))
                .totalExonerado(getBigDecimalValue(jsonNode, "totalExonerado"))
                .totalVentas(getBigDecimalValue(jsonNode, "totalVentas"))
                .totalDescuentos(getBigDecimalValue(jsonNode, "totalDescuentos"))
                .totalVentasNeta(getBigDecimalValue(jsonNode, "totalVentasNeta"))
                .totalImp(getBigDecimalValue(jsonNode, "totalImp"))
                .totalIVADevuelto(getBigDecimalValue(jsonNode, "totalIVADevuelto"))
                .totalOtrosCargos(getBigDecimalValue(jsonNode, "totalOtrosCargos"))
                .totalComprobante(getBigDecimalValue(jsonNode, "totalComprobante"))
                .otros(getTextValue(jsonNode, "otros"))

                // Campos específicos para mensajes receptor
                .mensaje(getTextValue(jsonNode, "mensaje"))
                .detalleMensaje(getTextValue(jsonNode, "detalleMensaje"))
                .condicionImpuesto(getTextValue(jsonNode, "condicionImpuesto"))
                .montoTotalImpuestoAcreditar(getBigDecimalValue(jsonNode, "montoTotalImpuestoAcreditar"))
                .montoTotalDeGastoAplicable(getBigDecimalValue(jsonNode, "montoTotalDeGastoAplicable"))
                .montoTotalImpuesto(getBigDecimalValue(jsonNode, "montoTotalImpuesto"))
                .totalFactura(getBigDecimalValue(jsonNode, "totalFactura"))
                .claveDocumentoEmisor(getTextValue(jsonNode, "claveDocumentoEmisor"));

            // Mapear detalle de líneas
            if (jsonNode.has("detalleLinea")) {
                builder.detalleLinea(mapDetalleLineas(jsonNode.get("detalleLinea")));
            }

            // Mapear referencias
            if (jsonNode.has("referencias")) {
                builder.referencias(mapReferencias(jsonNode.get("referencias")));
            }

            // Mapear otros cargos
            if (jsonNode.has("otrosCargos")) {
                builder.otrosCargos(mapOtrosCargos(jsonNode.get("otrosCargos")));
            }

            FacturaRequestDTO result = builder.build();
            log.debug("Mapeo JSON completado exitosamente para tipo: {}", result.tipoDocumento());
            return result;

        } catch (Exception e) {
            log.error("Error mapeando JSON a FacturaRequestDTO: {}", e.getMessage(), e);
            throw new ValidationException("Error en formato JSON: " + e.getMessage());
        }
    }

    // ==================== Métodos Helper ====================

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        return fieldNode.isMissingNode() || fieldNode.isNull() ? null : fieldNode.asText();
    }

    private String getTextValueWithDefault(JsonNode node, String fieldName, String defaultValue) {
        String value = getTextValue(node, fieldName);
        return (value == null || value.trim().isEmpty()) ? defaultValue : value;
    }

    private Integer getIntValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        return fieldNode.isMissingNode() || fieldNode.isNull() ? null : fieldNode.asInt();
    }

    private BigDecimal getBigDecimalValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(fieldNode.asText());
        } catch (NumberFormatException e) {
            log.warn("Error parsing BigDecimal for field {}: {}", fieldName, fieldNode.asText());
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getBigDecimalValueWithDefault(JsonNode node, String fieldName, String defaultValue) {
        BigDecimal value = getBigDecimalValue(node, fieldName);
        return value.equals(BigDecimal.ZERO) ? new BigDecimal(defaultValue) : value;
    }

    // ==================== Mapeo de Estructuras Complejas ====================

    private List<DetalleLineaDTO> mapDetalleLineas(JsonNode detalleNode) {
        List<DetalleLineaDTO> detalles = new ArrayList<>();

        if (detalleNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = detalleNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode lineaNode = entry.getValue();

                DetalleLineaDTO detalle = mapLineaIndividual(lineaNode);
                detalles.add(detalle);
            }
        }

        return detalles;
    }

    private DetalleLineaDTO mapLineaIndividual(JsonNode lineaNode) {
        return DetalleLineaDTO.builder()
            .numeroLinea(lineaNode.path("numeroLinea").asInt())
            .partidaArancelaria(getTextValue(lineaNode, "partidaArancelaria"))
            .codigo(getTextValue(lineaNode, "codigo"))
            .codigoComercial(mapCodigosComerciales(lineaNode))
            .cantidad(getBigDecimalValue(lineaNode, "cantidad"))
            .unidadMedida(getTextValue(lineaNode, "unidadMedida"))
            .unidadMedidaComercial(getTextValue(lineaNode, "unidadMedidaComercial"))
            .detalle(getTextValue(lineaNode, "detalle"))
            .precioUnitario(getBigDecimalValue(lineaNode, "precioUnitario"))
            .montoTotal(getBigDecimalValue(lineaNode, "montoTotal"))
            .descuentos(mapDescuentos(lineaNode))
            .subTotal(getBigDecimalValue(lineaNode, "subTotal"))
            .impuestos(mapImpuestos(lineaNode))
            .impuestoNeto(getBigDecimalValue(lineaNode, "impuestoNeto"))
            .montoTotalLinea(getBigDecimalValue(lineaNode, "montoTotalLinea"))
            .exoneraciones(mapExoneraciones(lineaNode))
            .build();
    }

    private List<CodigoComercialDTO> mapCodigosComerciales(JsonNode lineaNode) {
        List<CodigoComercialDTO> codigos = new ArrayList<>();

        // Mapear códigos comerciales del 1 al 5
        for (int i = 1; i <= 5; i++) {
            String tipoField = i == 1 ? "codigoComercialTipo" : "codigoComercialTipo" + i;
            String codigoField = i == 1 ? "codigoComercialCodigo" : "codigoComercialCodigo" + i;

            String tipo = getTextValue(lineaNode, tipoField);
            String codigo = getTextValue(lineaNode, codigoField);

            if (tipo != null && codigo != null) {
                codigos.add(new CodigoComercialDTO(tipo, codigo));
            }
        }

        return codigos;
    }

    private List<DescuentoDTO> mapDescuentos(JsonNode lineaNode) {
        List<DescuentoDTO> descuentos = new ArrayList<>();

        BigDecimal montoDescuento = getBigDecimalValue(lineaNode, "montoDescuento");
        String naturalezaDescuento = getTextValue(lineaNode, "naturalezaDescuento");

        if (montoDescuento.compareTo(BigDecimal.ZERO) > 0) {
            // Crear DTO de descuento básico - ajustar según tu estructura real
            descuentos.add(new DescuentoDTO(
                montoDescuento,
                naturalezaDescuento != null ? naturalezaDescuento : "Descuento comercial"
            ));
        }

        return descuentos;
    }

    private List<ImpuestoItemDTO> mapImpuestos(JsonNode lineaNode) {
        List<ImpuestoItemDTO> impuestos = new ArrayList<>();

        // Mapear impuesto base si existe
        BigDecimal impuestoMonto = getBigDecimalValue(lineaNode, "impuesto");
        if (impuestoMonto.compareTo(BigDecimal.ZERO) > 0) {
            // Crear DTO de impuesto básico - ajustar según tu estructura real
            impuestos.add(new ImpuestoItemDTO(
                "01", // IVA por defecto
                new BigDecimal("13"), // Tarifa general por defecto
                "08", //Codigo General IVA
                new BigDecimal("13"),
                impuestoMonto,
                null // Sin exoneración por defecto
            ));
        }

        return impuestos;
    }

    private List<ExoneracionDTO> mapExoneraciones(JsonNode lineaNode) {
        List<ExoneracionDTO> exoneraciones = new ArrayList<>();

        // Mapear exoneración si existe
        BigDecimal montoExoneracion = getBigDecimalValue(lineaNode, "montoExoneracion");
        Integer montoExoneracionInt = montoExoneracion.intValue();
        if (montoExoneracion.compareTo(BigDecimal.ZERO) > 0) {
            String tipoDocumento = getTextValue(lineaNode, "tipoDocumentoExoneracion");
            String numeroDocumento = getTextValue(lineaNode, "numeroDocumentoExoneracion");

            if (tipoDocumento != null && numeroDocumento != null) {
                exoneraciones.add(new ExoneracionDTO(
                    tipoDocumento,
                    numeroDocumento,
                    getTextValue(lineaNode, "nombreInstitucion"),
                    getTextValue(lineaNode, "fechaEmisionExoneracion"),
                    getBigDecimalValue(lineaNode, "porcentajeExoneracion"),
                    montoExoneracionInt
                ));
            }
        }

        return exoneraciones;
    }

    private List<ReferenciaDTO> mapReferencias(JsonNode referenciasNode) {
        List<ReferenciaDTO> referencias = new ArrayList<>();

        if (referenciasNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = referenciasNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode referenciaNode = entry.getValue();

                referencias.add(new ReferenciaDTO(
                    getTextValue(referenciaNode, "numero"),
                    getTextValue(referenciaNode, "fechaEmision"),
                    getTextValue(referenciaNode, "codigo"),
                    getTextValue(referenciaNode, "razon")
                ));
            }
        }

        return referencias;
    }

    private List<OtroCargoDTO> mapOtrosCargos(JsonNode otrosCargosNode) {
        List<OtroCargoDTO> otrosCargos = new ArrayList<>();

        if (otrosCargosNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = otrosCargosNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode cargoNode = entry.getValue();

                otrosCargos.add(new OtroCargoDTO(
                    getTextValue(cargoNode, "tipoDocumento"),
                    getTextValue(cargoNode, "numeroIdentidadTercero"),
                    getTextValue(cargoNode, "nombreTercero"),
                    getTextValue(cargoNode, "detalle"),
                    getTextValue(cargoNode, "porcentaje"),
                    getBigDecimalValue(cargoNode, "montoCargo")
                ));
            }
        }

        return otrosCargos;
    }
}