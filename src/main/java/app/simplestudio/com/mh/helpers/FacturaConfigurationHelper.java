package app.simplestudio.com.mh.helpers;

import app.simplestudio.com.mh.CCampoFactura;
import app.simplestudio.com.mh.FuncionesService;
import app.simplestudio.com.models.entity.Factura;
import org.springframework.stereotype.Component;

@Component
public class FacturaConfigurationHelper {
    
    /**
     * Configura los datos básicos de la factura desde CCampoFactura
     */
    public void configurarDatosBasicos(Factura factura, CCampoFactura c, String tipoDocumento,
                                      String situacion, String sucursal, String terminal,
                                      String emisor, FuncionesService funcionesService) {
        factura.setTipoDocumento(tipoDocumento);
        factura.setClave(c.getClave());
        factura.setConsecutivo(c.getConsecutivo());
        factura.setCodigoActividad(c.getCodigoActividad());
        factura.setFechaEmision(c.getFechaEmision());
        
        // Datos del emisor
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
        
        // Datos generales
        factura.setIdentificacion(emisor);
        factura.setSituacion(situacion);
        factura.setTerminal(funcionesService.str_pad(terminal, 5, "0", "STR_PAD_LEFT"));
        factura.setSucursal(funcionesService.str_pad(sucursal, 3, "0", "STR_PAD_LEFT"));
        
        // Datos del receptor
        factura.setOmitirReceptor(c.getOmitirReceptor());
        factura.setReceptorNombre(c.getReceptorNombre());
        factura.setReceptorTipoIdentif(c.getReceptorTipoIdentif());
        factura.setReceptor_num_identif(c.getReceptorNumIdentif());
        factura.setReceptorProvincia(c.getReceptorProvincia());
        factura.setReceptorCanton(c.getReceptorCanton());
        factura.setReceptorDistrito(c.getReceptorDistrito());
        factura.setReceptorBarrio(c.getReceptorBarrio());
        factura.setReceptorOtrasSenas(c.getReceptorOtrasSenas());
        factura.setOtrasSenasExtranjero(c.getOtrasSenasExtranjero());
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
    }
    
    /**
     * Configura los totales de la factura desde CCampoFactura
     */
    public void configurarTotales(Factura factura, CCampoFactura c) {
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
    }
}