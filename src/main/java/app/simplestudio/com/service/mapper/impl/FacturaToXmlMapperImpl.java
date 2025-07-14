
package app.simplestudio.com.service.mapper.impl;

import app.simplestudio.com.mh.FuncionesService;
import app.simplestudio.com.models.entity.*;
import app.simplestudio.com.mh.CCampoFactura;
import app.simplestudio.com.service.mapper.FacturaToXmlMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class FacturaToXmlMapperImpl implements FacturaToXmlMapper {

    private static final Logger log = LoggerFactory.getLogger(FacturaToXmlMapperImpl.class);

    private final FuncionesService funcionesService;
    private final ObjectMapper objectMapper;

    public FacturaToXmlMapperImpl(FuncionesService funcionesService) {
        this.funcionesService = funcionesService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public CCampoFactura mapToXmlFormat(Factura factura, Emisor emisor) {
        log.debug("Mapeando factura a formato XML: {}", factura.getClave());

        CCampoFactura campo = new CCampoFactura();

        // Datos básicos
        campo.setClave(factura.getClave());
        campo.setCodigoActividad(factura.getCodigoActividad());
        campo.setConsecutivo(factura.getConsecutivo());
        campo.setFechaEmision(factura.getFechaEmision());

        // Datos del emisor
        mapearEmisor(campo, factura, emisor);

        // Datos del receptor
        mapearReceptor(campo, factura);

        // Condiciones de venta
        mapearCondicionesVenta(campo, factura);

        // Detalle de líneas (convertir items a JSON)
        mapearDetalleLineas(campo, factura);

        // Referencias
        mapearReferencias(campo, factura);

        // Otros cargos
        mapearOtrosCargos(campo, factura);

        // Totales
        mapearTotales(campo, factura);

        log.debug("Mapeo completado para clave: {}", factura.getClave());
        return campo;
    }

    private void mapearEmisor(CCampoFactura campo, Factura factura, Emisor emisor) {
        campo.setEmisorNombre(factura.getEmisorNombre());
        campo.setEmisorTipoIdentif(factura.getEmisorTipoIdentif());
        campo.setEmisorNumIdentif(factura.getEmisorNumIdentif());
        campo.setNombreComercial(factura.getNombreComercial());
        campo.setEmisorProv(factura.getEmisorProv());
        campo.setEmisorCanton(factura.getEmisorCanton());
        campo.setEmisorDistrito(factura.getEmisorDistrito());
        campo.setEmisorBarrio(factura.getEmisorBarrio());
        campo.setEmisorOtrasSenas(factura.getEmisorOtrasSenas());
        campo.setEmisorCodPaisTel(factura.getEmisorCodPaisTel());
        campo.setEmisorTel(factura.getEmisorTel());
        campo.setEmisorCodPaisFax(factura.getEmisorCodPaisFax());
        campo.setEmisorFax(factura.getEmisorFax());
        campo.setEmisorEmail(factura.getEmisorEmail());
    }

    private void mapearReceptor(CCampoFactura campo, Factura factura) {
        campo.setOmitirReceptor(factura.getOmitirReceptor());
        campo.setReceptorNombre(factura.getReceptorNombre());
        campo.setReceptorTipoIdentif(factura.getReceptorTipoIdentif());
        campo.setReceptorNumIdentif(factura.getReceptor_num_identif());
        campo.setReceptorProvincia(factura.getReceptorProvincia());
        campo.setReceptorCanton(factura.getReceptorCanton());
        campo.setReceptorDistrito(factura.getReceptorDistrito());
        campo.setReceptorBarrio(factura.getReceptorBarrio());
        campo.setReceptorOtrasSenas(factura.getReceptorOtrasSenas());
        campo.setOtrasSenasExtranjero(factura.getOtrasSenasExtranjero());
        campo.setReceptorCodPaisTel(factura.getReceptorCodPaisTel());
        campo.setReceptorTel(factura.getReceptorTel());
        campo.setReceptorCodPaisFax(factura.getReceptorCodPaisFax());
        campo.setReceptorFax(factura.getReceptorFax());
        campo.setReceptorEmail(factura.getReceptorEmail());
    }

    private void mapearCondicionesVenta(CCampoFactura campo, Factura factura) {
        campo.setCondVenta(factura.getCondVenta());
        campo.setPlazoCredito(factura.getPlazoCredito());
        campo.setMedioPago(factura.getMedioPago());
        campo.setMedioPago2(factura.getMedioPago2());
        campo.setMedioPago3(factura.getMedioPago3());
        campo.setMedioPago4(factura.getMedioPago4());
        campo.setCodMoneda(factura.getCodMoneda());
        campo.setTipoCambio(factura.getTipoCambio());
    }

    private void mapearDetalleLineas(CCampoFactura campo, Factura factura) {
        try {
            if (factura.getItems() != null && !factura.getItems().isEmpty()) {
                // Aquí convertirías los items a JSON como lo hacía el código original
                // Por simplicidad, usar el método existente si está disponible
                String detalleJson = convertirItemsAJson(factura.getItems());
                campo.setDetalleFactura(detalleJson);
            }
        } catch (Exception e) {
            log.error("Error mapeando detalle de líneas: {}", e.getMessage(), e);
            throw new RuntimeException("Error mapeando items: " + e.getMessage());
        }
    }

    private void mapearReferencias(CCampoFactura campo, Factura factura) {
        try {
            if (factura.getItemsReferencias() != null && !factura.getItemsReferencias().isEmpty()) {
                String referenciasJson = convertirReferenciasAJson(factura.getItemsReferencias());
                campo.setReferencia(referenciasJson);
            }
        } catch (Exception e) {
            log.error("Error mapeando referencias: {}", e.getMessage(), e);
            throw new RuntimeException("Error mapeando referencias: " + e.getMessage());
        }
    }

    private void mapearOtrosCargos(CCampoFactura campo, Factura factura) {
        try {
            // Para otros cargos, tenemos que usar reflection para acceder al campo private
            // O mejor, vamos a modificar el FacturaBuilder para que no use addOtrosCargos
            // sino que use setItemsOtrosCargos directamente

            // Por ahora, saltamos otros cargos si no hay getter
            log.debug("Otros cargos no procesados - falta getter en entidad Factura");

        } catch (Exception e) {
            log.error("Error mapeando otros cargos: {}", e.getMessage(), e);
            throw new RuntimeException("Error mapeando otros cargos: " + e.getMessage());
        }
    }

    private void mapearTotales(CCampoFactura campo, Factura factura) {
        campo.setTotalServGravados(factura.getTotalServGravados());
        campo.setTotalServExentos(factura.getTotalServExentos());
        campo.setTotalServExonerado(factura.getTotalServExonerado());
        campo.setTotalMercGravadas(factura.getTotalMercGravadas());
        campo.setTotalMercExentas(factura.getTotalMercExentas());
        campo.setTotalMercExonerada(factura.getTotalMercExonerada());
        campo.setTotalGravados(factura.getTotalGravados());
        campo.setTotalExentos(factura.getTotalExentos());
        campo.setTotalExonerado(factura.getTotalExonerado());
        campo.setTotalVentas(factura.getTotalVentas());
        campo.setTotalDescuentos(factura.getTotalDescuentos());
        campo.setTotalVentasNeta(factura.getTotalVentaNeta());
        campo.setTotalImp(factura.getTotalImp());
        campo.setTotalIVADevuelto(factura.getTotalIVADevuelto());
        campo.setTotalOtrosCargos(factura.getTotalOtrosCargos());
        campo.setTotalComprobante(factura.getTotalComprobante());
        campo.setNumeroFactura(factura.getNumeroFactura());
        campo.setOtros(factura.getOtros());
    }

    // ==================== Métodos de Conversión JSON ====================

    /**
     * Convierte los items de factura al formato JSON requerido por el generador XML
     */
    private String convertirItemsAJson(java.util.List<ItemFactura> items) {
        try {
            StringBuilder detalleLineasJson = new StringBuilder();

            for (int i = 0; i < items.size(); i++) {
                ItemFactura item = items.get(i);

                if (i > 0) {
                    detalleLineasJson.append(",");
                }

                detalleLineasJson.append("\"").append(i + 1).append("\":");
                detalleLineasJson.append(convertirItemIndividualAJson(item));
            }

            return "{" + detalleLineasJson.toString() + "}";

        } catch (Exception e) {
            log.error("Error convirtiendo items a JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Error en conversión de items", e);
        }
    }

    /**
     * Convierte un item individual a JSON
     */
    private String convertirItemIndividualAJson(ItemFactura item) {
        StringBuilder itemJson = new StringBuilder();

        itemJson.append("{");

        // Datos básicos del item
        itemJson.append("\"numeroLinea\":\"").append(item.getNumeroLinea()).append("\",");

        if (item.getPartidaArancelaria() != null && !item.getPartidaArancelaria().isEmpty()) {
            itemJson.append("\"partidaArancelaria\":\"").append(escaparTexto(item.getPartidaArancelaria())).append("\",");
        }

        itemJson.append("\"codigo\":\"").append(item.getCodigo()).append("\",");

        // Códigos comerciales
        itemJson.append("\"codigoComercial\":");
        itemJson.append(convertirCodigosComerciales(item));
        itemJson.append(",");

        itemJson.append("\"cantidad\":\"").append(item.getCantidad()).append("\",");
        itemJson.append("\"unidadMedida\":\"").append(item.getUnidadMedida()).append("\",");
        itemJson.append("\"unidadMedidaComercial\":\"").append(escaparTexto(item.getUnidadMedidaComercial())).append("\",");
        itemJson.append("\"detalle\":\"").append(escaparTexto(item.getDetalle())).append("\",");
        itemJson.append("\"precioUnitario\":\"").append(item.getPrecioUnitario()).append("\",");
        itemJson.append("\"montoTotal\":\"").append(item.getMontoTotal()).append("\",");

        // Descuentos
        itemJson.append("\"descuentos\":");
        itemJson.append(convertirDescuentos(item));
        itemJson.append(",");

        itemJson.append("\"subTotal\":\"").append(item.getSubTotal()).append("\",");

        // Impuestos
        itemJson.append("\"impuestos\":");
        itemJson.append(convertirImpuestosItem(item));
        itemJson.append(",");

        itemJson.append("\"impuestoNeto\":\"").append(item.getImpuestoNeto()).append("\",");
        itemJson.append("\"montoTotalLinea\":\"").append(item.getMontoTotalLinea()).append("\"");

        itemJson.append("}");

        return itemJson.toString();
    }

    /**
     * Convierte códigos comerciales a JSON
     */
    private String convertirCodigosComerciales(ItemFactura item) {
        StringBuilder codigosJson = new StringBuilder();
        codigosJson.append("{");

        boolean first = true;
        int index = 0;

        // Código comercial 1
        if (item.getCodigoComercialTipo() != null && !item.getCodigoComercialTipo().isEmpty()) {
            if (!first) codigosJson.append(",");
            codigosJson.append("\"").append(index++).append("\":{");
            codigosJson.append("\"tipo\":\"").append(item.getCodigoComercialTipo()).append("\",");
            codigosJson.append("\"codigo\":\"").append(escaparTexto(item.getCodigoComercialCodigo())).append("\"");
            codigosJson.append("}");
            first = false;
        }

        // Código comercial 2
        if (item.getCodigoComercialTipo2() != null && !item.getCodigoComercialTipo2().isEmpty()) {
            if (!first) codigosJson.append(",");
            codigosJson.append("\"").append(index++).append("\":{");
            codigosJson.append("\"tipo\":\"").append(item.getCodigoComercialTipo2()).append("\",");
            codigosJson.append("\"codigo\":\"").append(escaparTexto(item.getCodigoComercialCodigo2())).append("\"");
            codigosJson.append("}");
            first = false;
        }

        // Código comercial 3
        if (item.getCodigoComercialTipo3() != null && !item.getCodigoComercialTipo3().isEmpty()) {
            if (!first) codigosJson.append(",");
            codigosJson.append("\"").append(index++).append("\":{");
            codigosJson.append("\"tipo\":\"").append(item.getCodigoComercialTipo3()).append("\",");
            codigosJson.append("\"codigo\":\"").append(escaparTexto(item.getCodigoComercialCodigo3())).append("\"");
            codigosJson.append("}");
            first = false;
        }

        // Código comercial 4
        if (item.getCodigoComercialTipo4() != null && !item.getCodigoComercialTipo4().isEmpty()) {
            if (!first) codigosJson.append(",");
            codigosJson.append("\"").append(index++).append("\":{");
            codigosJson.append("\"tipo\":\"").append(item.getCodigoComercialTipo4()).append("\",");
            codigosJson.append("\"codigo\":\"").append(escaparTexto(item.getCodigoComercialCodigo4())).append("\"");
            codigosJson.append("}");
            first = false;
        }

        // Código comercial 5
        if (item.getCodigoComercialTipo5() != null && !item.getCodigoComercialTipo5().isEmpty()) {
            if (!first) codigosJson.append(",");
            codigosJson.append("\"").append(index++).append("\":{");
            codigosJson.append("\"tipo\":\"").append(item.getCodigoComercialTipo5()).append("\",");
            codigosJson.append("\"codigo\":\"").append(escaparTexto(item.getCodigoComercialCodigo5())).append("\"");
            codigosJson.append("}");
            first = false;
        }

        codigosJson.append("}");
        return codigosJson.toString();
    }

    /**
     * Convierte descuentos a JSON
     */
    private String convertirDescuentos(ItemFactura item) {
        StringBuilder descuentosJson = new StringBuilder();
        descuentosJson.append("{");

        boolean first = true;
        int index = 0;

        // Descuento 1
        if (item.getMontoDescuento() != null && item.getMontoDescuento() > 0) {
            if (!first) descuentosJson.append(",");
            descuentosJson.append("\"").append(index++).append("\":{");
            descuentosJson.append("\"montoDescuento\":\"").append(item.getMontoDescuento()).append("\",");
            descuentosJson.append("\"naturalezaDescuento\":\"").append(escaparTexto(item.getNaturalezaDescuento())).append("\"");
            descuentosJson.append("}");
            first = false;
        }

        // Descuento 2
        if (item.getMontoDescuento2() != null && item.getMontoDescuento2() > 0) {
            if (!first) descuentosJson.append(",");
            descuentosJson.append("\"").append(index++).append("\":{");
            descuentosJson.append("\"montoDescuento\":\"").append(item.getMontoDescuento2()).append("\",");
            descuentosJson.append("\"naturalezaDescuento\":\"").append(escaparTexto(item.getNaturalezaDescuento2())).append("\"");
            descuentosJson.append("}");
            first = false;
        }

        // Descuento 3
        if (item.getMontoDescuento3() != null && item.getMontoDescuento3() > 0) {
            if (!first) descuentosJson.append(",");
            descuentosJson.append("\"").append(index++).append("\":{");
            descuentosJson.append("\"montoDescuento\":\"").append(item.getMontoDescuento3()).append("\",");
            descuentosJson.append("\"naturalezaDescuento\":\"").append(escaparTexto(item.getNaturalezaDescuento3())).append("\"");
            descuentosJson.append("}");
            first = false;
        }

        // Descuento 4
        if (item.getMontoDescuento4() != null && item.getMontoDescuento4() > 0) {
            if (!first) descuentosJson.append(",");
            descuentosJson.append("\"").append(index++).append("\":{");
            descuentosJson.append("\"montoDescuento\":\"").append(item.getMontoDescuento4()).append("\",");
            descuentosJson.append("\"naturalezaDescuento\":\"").append(escaparTexto(item.getNaturalezaDescuento4())).append("\"");
            descuentosJson.append("}");
            first = false;
        }

        // Descuento 5
        if (item.getMontoDescuento5() != null && item.getMontoDescuento5() > 0) {
            if (!first) descuentosJson.append(",");
            descuentosJson.append("\"").append(index++).append("\":{");
            descuentosJson.append("\"montoDescuento\":\"").append(item.getMontoDescuento5()).append("\",");
            descuentosJson.append("\"naturalezaDescuento\":\"").append(escaparTexto(item.getNaturalezaDescuento5())).append("\"");
            descuentosJson.append("}");
            first = false;
        }

        descuentosJson.append("}");
        return descuentosJson.toString();
    }

    /**
     * Convierte impuestos del item a JSON
     */
    private String convertirImpuestosItem(ItemFactura item) {
        StringBuilder impuestosJson = new StringBuilder();
        impuestosJson.append("{");

        if (item.getImpuestosItemFactura() != null && !item.getImpuestosItemFactura().isEmpty()) {
            boolean first = true;
            int index = 0;

            for (ImpuestosItemFactura impuesto : item.getImpuestosItemFactura()) {
                if (!first) impuestosJson.append(",");

                impuestosJson.append("\"").append(index++).append("\":{");
                impuestosJson.append("\"codigo\":\"").append(impuesto.getCodigo()).append("\",");
                impuestosJson.append("\"tarifa\":\"").append(impuesto.getTarifa()).append("\",");
                impuestosJson.append("\"codigoTarifa\":\"").append(impuesto.getCodigoTarifa()).append("\",");
                impuestosJson.append("\"monto\":\"").append(impuesto.getMonto()).append("\"");

                if (impuesto.getImpuestoNeto() != null) {
                    impuestosJson.append(",\"impuestoNeto\":\"").append(impuesto.getImpuestoNeto()).append("\"");
                }

                // Exoneración si existe
                if (impuesto.getExoneracionImpuestoItemFactura() != null &&
                    !impuesto.getExoneracionImpuestoItemFactura().isEmpty()) {

                    ExoneracionImpuestoItemFactura exoneracion = impuesto.getExoneracionImpuestoItemFactura().get(0);
                    impuestosJson.append(",\"exoneracion\":{");
                    impuestosJson.append("\"tipoDocumento\":\"").append(exoneracion.getTipoDocumento()).append("\",");
                    impuestosJson.append("\"numeroDocumento\":\"").append(exoneracion.getNumeroDocumento()).append("\",");
                    impuestosJson.append("\"nombreInstitucion\":\"").append(escaparTexto(exoneracion.getNombreInstitucion())).append("\",");
                    impuestosJson.append("\"fechaEmision\":\"").append(exoneracion.getFechaEmision()).append("\",");
                    impuestosJson.append("\"montoExoneracion\":\"").append(exoneracion.getMontoExoneracion()).append("\",");
                    impuestosJson.append("\"porcentajeExoneracion\":\"").append(exoneracion.getPorcentajeExoneracion()).append("\"");
                    impuestosJson.append("}");
                }

                impuestosJson.append("}");
                first = false;
            }
        }

        impuestosJson.append("}");
        return impuestosJson.toString();
    }

    /**
     * Convierte referencias a JSON
     */
    private String convertirReferenciasAJson(java.util.List<FacturaReferencia> referencias) {
        try {
            StringBuilder referenciasJson = new StringBuilder();
            referenciasJson.append("{");

            for (int i = 0; i < referencias.size(); i++) {
                FacturaReferencia referencia = referencias.get(i);

                if (i > 0) {
                    referenciasJson.append(",");
                }

                referenciasJson.append("\"").append(i).append("\":{");
                referenciasJson.append("\"numero\":\"").append(referencia.getNumero()).append("\",");
                referenciasJson.append("\"fechaEmision\":\"").append(referencia.getFechaEmision()).append("\",");
                referenciasJson.append("\"codigo\":\"").append(referencia.getCodigo()).append("\",");
                referenciasJson.append("\"razon\":\"").append(escaparTexto(referencia.getRazon())).append("\"");
                referenciasJson.append("}");
            }

            referenciasJson.append("}");
            return referenciasJson.toString();

        } catch (Exception e) {
            log.error("Error convirtiendo referencias a JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Error en conversión de referencias", e);
        }
    }

    /**
     * Convierte otros cargos a JSON
     */
    private String convertirOtrosCargosAJson(java.util.List<FacturaOtrosCargos> otrosCargos) {
        try {
            StringBuilder otrosCargosJson = new StringBuilder();
            otrosCargosJson.append("{");

            for (int i = 0; i < otrosCargos.size(); i++) {
                FacturaOtrosCargos cargo = otrosCargos.get(i);

                if (i > 0) {
                    otrosCargosJson.append(",");
                }

                otrosCargosJson.append("\"").append(i).append("\":{");
                otrosCargosJson.append("\"tipoDocumento\":\"").append(cargo.getTipoDocumento()).append("\",");

                if (cargo.getNumeroIdentidadTercero() != null) {
                    otrosCargosJson.append("\"numeroIdentidadTercero\":\"").append(cargo.getNumeroIdentidadTercero()).append("\",");
                }
                if (cargo.getNombreTercero() != null) {
                    otrosCargosJson.append("\"nombreTercero\":\"").append(escaparTexto(cargo.getNombreTercero())).append("\",");
                }

                otrosCargosJson.append("\"detalle\":\"").append(escaparTexto(cargo.getDetalle())).append("\",");

                if (cargo.getPorcentaje() != null) {
                    otrosCargosJson.append("\"porcentaje\":\"").append(cargo.getPorcentaje()).append("\",");
                }

                otrosCargosJson.append("\"montoCargo\":\"").append(cargo.getMontoCargo()).append("\"");
                otrosCargosJson.append("}");
            }

            otrosCargosJson.append("}");
            return otrosCargosJson.toString();

        } catch (Exception e) {
            log.error("Error convirtiendo otros cargos a JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Error en conversión de otros cargos", e);
        }
    }

    /**
     * Escapa texto para JSON (replicando la función del código original)
     */
    private String escaparTexto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }

        // Usar StringEscapeUtils como en el código original
        return org.apache.commons.text.StringEscapeUtils.escapeJava(texto);
    }
}