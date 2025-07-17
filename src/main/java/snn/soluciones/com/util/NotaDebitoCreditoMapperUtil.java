package snn.soluciones.com.util;


import snn.soluciones.com.dto.ConsecutiveCalculationResult;
import snn.soluciones.com.mh.CCampoFactura;
import snn.soluciones.com.mh.FuncionesService;
import snn.soluciones.com.models.entity.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class NotaDebitoCreditoMapperUtil {

    @Autowired private FuncionesService funcionesService;

    public CCampoFactura buildCCampoFactura(JsonNode requestNode, Emisor emisor,
        ConsecutiveCalculationResult consecutiveResult,
        String tipoDocumento) {
        CCampoFactura c = new CCampoFactura();

        // Datos básicos
        c.setTipoDocumento(tipoDocumento);
        c.setConsecutivo(funcionesService.str_pad(consecutiveResult.getConsecutivoFinal().toString(), 10, "0", "STR_PAD_LEFT"));

        // Generar clave
        SimpleDateFormat formatFecha = new SimpleDateFormat("ddMMyyyy");
        SimpleDateFormat formatHora = new SimpleDateFormat("HHmmss");
        Date now = new Date();

        String clave = "506" +
            formatFecha.format(now) +
            emisor.getIdentificacion() +
            c.getConsecutivo() +
            requestNode.path("situacion").asText() +
            requestNode.path("codigoSeguridad").asText();

        c.setClave(clave);
        c.setCodigoActividad(requestNode.path("codigoActividad").asText());
        c.setFechaEmision(formatFecha.format(now) + "T" + formatHora.format(now) + "-06:00");

        // Datos del emisor
        mapEmisorData(c, emisor, requestNode);

        return c;
    }

    private void mapEmisorData(CCampoFactura c, Emisor emisor, JsonNode requestNode) {
        c.setEmisorNombre(emisor.getNombreRazonSocial());
        c.setEmisorTipoIdentif(emisor.getTipoDeIdentificacion().getTipoDeIdentificacion());
        c.setEmisorNumIdentif(emisor.getIdentificacion());
        c.setNombreComercial(emisor.getNombreComercial());

        // Dirección del emisor
        c.setEmisorProv(funcionesService.str_pad(emisor.getProvincia().getProvincia(), 1, "0", "STR_PAD_LEFT"));
        c.setEmisorCanton(funcionesService.str_pad(emisor.getCanton().getNumeroCanton(), 2, "0", "STR_PAD_LEFT"));
        c.setEmisorDistrito(funcionesService.str_pad(emisor.getDistrito().getNumeroDistrito(), 2, "0", "STR_PAD_LEFT"));
        c.setEmisorBarrio(funcionesService.str_pad(emisor.getBarrio().getNumeroBarrio(), 2, "0", "STR_PAD_LEFT"));
        c.setEmisorOtrasSenas(emisor.getOtrasSenas());

        // Contacto del emisor
        c.setEmisorCodPaisTel(emisor.getCodigoPais());
        c.setEmisorTel(emisor.getTelefono());
        c.setEmisorCodPaisFax(emisor.getCodigoPais());
        c.setEmisorFax(emisor.getFax());
        c.setEmisorEmail(emisor.getEmail());

        // Situación, sucursal y terminal
        c.setSituacion(requestNode.path("situacion").asText());
        c.setTerminal(funcionesService.str_pad(requestNode.path("terminal").asText(), 5, "0", "STR_PAD_LEFT"));
        c.setSucursal(funcionesService.str_pad(requestNode.path("sucursal").asText(), 3, "0", "STR_PAD_LEFT"));
    }

    public void mapReceptorData(JsonNode requestNode, CCampoFactura campoFactura) {
        // Mapeo de datos del receptor - REEMPLAZA 50+ LÍNEAS DE SETTERS MANUALES
        campoFactura.setOmitirReceptor(requestNode.path("omitirReceptor").asText());
        campoFactura.setReceptorNombre(requestNode.path("receptorNombre").asText());
        campoFactura.setReceptorTipoIdentif(requestNode.path("receptorTipoIdentif").asText());
        campoFactura.setReceptorNumIdentif(requestNode.path("receptorNumIdentif").asText());

        // Dirección del receptor (solo si no es identificación extranjera)
        if (!requestNode.path("receptorTipoIdentif").asText().equals("05")) {
            if (hasValidAddress(requestNode)) {
                campoFactura.setReceptorProvincia(requestNode.path("receptorProvincia").asText());
                campoFactura.setReceptorCanton(requestNode.path("receptorCanton").asText());
                campoFactura.setReceptorDistrito(requestNode.path("receptorDistrito").asText());

                if (requestNode.has("receptorBarrio") && !requestNode.path("receptorBarrio").asText().isEmpty()) {
                    campoFactura.setReceptorBarrio(requestNode.path("receptorBarrio").asText());
                }

                campoFactura.setReceptorOtrasSenas(requestNode.path("receptorOtrasSenas").asText());
            }
        }

        // Contacto del receptor
        campoFactura.setReceptorCodPaisTel(requestNode.path("receptorCodPaisTel").asText());
        campoFactura.setReceptorTel(requestNode.path("receptorTel").asText());
        campoFactura.setReceptorCodPaisFax(requestNode.path("receptorCodPaisFax").asText());
        campoFactura.setReceptorFax(requestNode.path("receptorFax").asText());
        campoFactura.setReceptorEmail(requestNode.path("receptorEmail").asText());

        // Condiciones de venta
        campoFactura.setCondVenta(requestNode.path("condVenta").asText());
        campoFactura.setPlazoCredito(requestNode.path("plazoCredito").asText());
        campoFactura.setMedioPago(requestNode.path("medioPago").asText());
        campoFactura.setMedioPago2(requestNode.path("medioPago2").asText());
        campoFactura.setMedioPago3(requestNode.path("medioPago3").asText());
        campoFactura.setMedioPago4(requestNode.path("medioPago4").asText());

        // Moneda
        campoFactura.setCodMoneda(requestNode.path("codMoneda").asText());
        campoFactura.setTipoCambio(requestNode.path("tipoCambio").asText());
    }

    private boolean hasValidAddress(JsonNode requestNode) {
        return requestNode.has("receptorProvincia") && !requestNode.path("receptorProvincia").asText().isEmpty() &&
            requestNode.has("receptorCanton") && !requestNode.path("receptorCanton").asText().isEmpty() &&
            requestNode.has("receptorDistrito") && !requestNode.path("receptorDistrito").asText().isEmpty() &&
            requestNode.has("receptorOtrasSenas") && !requestNode.path("receptorOtrasSenas").asText().isEmpty();
    }

    /**
     * REEMPLAZA LAS 100+ LÍNEAS DE SETTERS MANUALES PARA ENTIDAD FACTURA
     * factura.setEmisorNombre(c.getEmisorNombre());
     * factura.setEmisorTipoIdentif(c.getEmisorTipoIdentif());
     * etc... por 100+ líneas más
     */
    public Factura mapToFacturaEntity(CCampoFactura c) {
        Factura factura = new Factura();

        // Mapeo automático usando reflection o manualmente pero organizado
        factura.setTipoDocumento(c.getTipoDocumento());
        factura.setClave(c.getClave());
        factura.setConsecutivo(c.getConsecutivo());
        factura.setCodigoActividad(c.getCodigoActividad());
        factura.setFechaEmision(c.getFechaEmision());

        // Emisor
        factura.setEmisorNombre(c.getEmisorNombre());
        factura.setEmisorTipoIdentif(c.getEmisorTipoIdentif());
        factura.setEmisorNumIdentif(c.getEmisorNumIdentif());
        factura.setNombreComercial(c.getNombreComercial());
        factura.setEmisorProv(c.getEmisorProv());
        factura.setEmisorCanton(c.getEmisorCanton());
        factura.setEmisorDistrito(c.getEmisorDistrito());
        factura.setEmisorBarrio(c.getEmisorBarrio());
        factura.setEmisorOtrasSenas(c.getEmisorOtrasSenas());
        factura.setEmisorCodPaisTel(c.getEmisorCodPaisTel());
        factura.setEmisorTel(c.getEmisorTel());
        factura.setEmisorCodPaisFax(c.getEmisorCodPaisFax());
        factura.setEmisorFax(c.getEmisorFax());
        factura.setEmisorEmail(c.getEmisorEmail());

        // Receptor
        factura.setOmitirReceptor(c.getOmitirReceptor());
        factura.setReceptorNombre(c.getReceptorNombre());
        factura.setReceptorTipoIdentif(c.getReceptorTipoIdentif());
        factura.setReceptor_num_identif(c.getReceptorNumIdentif());
        factura.setReceptorProvincia(c.getReceptorProvincia());
        factura.setReceptorCanton(c.getReceptorCanton());
        factura.setReceptorDistrito(c.getReceptorDistrito());
        factura.setReceptorBarrio(c.getReceptorBarrio());
        factura.setReceptorOtrasSenas(c.getReceptorOtrasSenas());
        factura.setReceptorCodPaisTel(c.getReceptorCodPaisTel());
        factura.setReceptorTel(c.getReceptorTel());
        factura.setReceptorCodPaisFax(c.getReceptorCodPaisFax());
        factura.setReceptorFax(c.getReceptorFax());
        factura.setReceptorEmail(c.getReceptorEmail());

        // Condiciones de venta
        factura.setCondVenta(c.getCondVenta());
        factura.setPlazoCredito(c.getPlazoCredito());
        factura.setMedioPago(c.getMedioPago());
        factura.setMedioPago2(c.getMedioPago2());
        factura.setMedioPago3(c.getMedioPago3());
        factura.setMedioPago4(c.getMedioPago4());
        factura.setCodMoneda(c.getCodMoneda());
        factura.setTipoCambio(c.getTipoCambio());

        // Totales
        factura.setTotalServGravados(c.getTotalServGravados());
        factura.setTotalServExentos(c.getTotalServExentos());
        factura.setTotalServExonerado(c.getTotalServExonerado());
        factura.setTotalMercGravadas(c.getTotalMercGravadas());
        factura.setTotalMercExentas(c.getTotalMercExentas());
        factura.setTotalMercExonerada(c.getTotalMercExonerada());
        factura.setTotalGravados(c.getTotalGravados());
        factura.setTotalExentos(c.getTotalExentos());
        factura.setTotalExonerado(c.getTotalExonerado());
        factura.setTotalVentas(c.getTotalVentas());
        factura.setTotalDescuentos(c.getTotalDescuentos());
        factura.setTotalVentaNeta(c.getTotalVentasNeta());
        factura.setTotalImp(c.getTotalImp());
        factura.setTotalIVADevuelto(c.getTotalIVADevuelto());
        factura.setTotalOtrosCargos(c.getTotalOtrosCargos());
        factura.setTotalComprobante(c.getTotalComprobante());
        factura.setOtros(c.getOtros());
        factura.setNumeroFactura(c.getNumeroFactura());

        // Otros campos
        factura.setIdentificacion(c.getIdentificacion());
        factura.setSituacion(c.getSituacion());
        factura.setTerminal(c.getTerminal());
        factura.setSucursal(c.getSucursal());

        return factura;
    }
}