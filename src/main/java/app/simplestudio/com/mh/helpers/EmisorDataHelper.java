package app.simplestudio.com.mh.helpers;

import app.simplestudio.com.mh.CCampoFactura;
import app.simplestudio.com.mh.FuncionesService;
import app.simplestudio.com.models.entity.Emisor;
import org.springframework.stereotype.Component;

@Component
public class EmisorDataHelper {
    
    /**
     * Configura los datos del emisor para documentos normales
     */
    public void configurarDatosEmisor(CCampoFactura c, Emisor e, FuncionesService funcionesService) {
        c.setEmisorNombre(e.getNombreRazonSocial());
        c.setEmisorTipoIdentif("0" + e.getTipoDeIdentificacion().getId().toString());
        c.setEmisorNumIdentif(e.getIdentificacion());
        
        if (e.getNombreComercial() != null) {
            c.setNombreComercial(e.getNombreComercial());
        }
        
        if (e.getProvincia() != null) {
            c.setEmisorProv(e.getProvincia().getId().toString());
        }
        
        if (e.getCanton() != null) {
            c.setEmisorCanton(funcionesService.str_pad(
                e.getCanton().getNumeroCanton(), 2, "0", "STR_PAD_LEFT"));
        }
        
        if (e.getDistrito() != null) {
            c.setEmisorDistrito(funcionesService.str_pad(
                e.getDistrito().getNumeroDistrito(), 2, "0", "STR_PAD_LEFT"));
        }
        
        if (e.getBarrio() != null) {
            c.setEmisorBarrio(funcionesService.str_pad(
                e.getBarrio().getNumeroBarrio(), 2, "0", "STR_PAD_LEFT"));
        }
        
        if (e.getOtrasSenas() != null) {
            c.setEmisorOtrasSenas(e.getOtrasSenas());
        }
        
        c.setEmisorCodPaisTel(e.getCodigoPais());
        c.setEmisorTel(e.getTelefono());
        c.setEmisorCodPaisFax(e.getCodigoPais());
        
        if (e.getFax() != null) {
            c.setEmisorFax(e.getFax());
        }
        
        c.setEmisorEmail(e.getEmail());
    }
    
    /**
     * Configura el emisor como receptor para Factura Electrónica de Compra (FEC)
     */
    public void configurarEmisorComoReceptor(CCampoFactura c, Emisor e, String emisorId, 
                                            FuncionesService funcionesService) {
        c.setReceptorNombre(e.getNombreRazonSocial());
        c.setReceptorTipoIdentif("0" + e.getTipoDeIdentificacion().getId().toString());
        c.setReceptorNumIdentif(emisorId);
        
        if (e.getProvincia() != null && e.getCanton() != null && 
            e.getDistrito() != null && e.getOtrasSenas() != null) {
            
            c.setReceptorProvincia(e.getProvincia().getId().toString());
            c.setReceptorCanton(funcionesService.str_pad(
                e.getCanton().getNumeroCanton(), 2, "0", "STR_PAD_LEFT"));
            c.setReceptorDistrito(funcionesService.str_pad(
                e.getDistrito().getNumeroDistrito(), 2, "0", "STR_PAD_LEFT"));
            
            if (e.getBarrio() != null) {
                c.setReceptorBarrio(funcionesService.str_pad(
                    e.getBarrio().getNumeroBarrio(), 2, "0", "STR_PAD_LEFT"));
            }
            
            c.setReceptorOtrasSenas(e.getOtrasSenas());
        }
        
        c.setReceptorCodPaisTel((e.getCodigoPais() != null) ? e.getCodigoPais() : "");
        c.setReceptorTel(e.getTelefono());
        c.setReceptorCodPaisFax(e.getCodigoPais());
        
        if (e.getFax() != null) {
            c.setReceptorFax(e.getFax());
        }
        
        c.setReceptorEmail(e.getEmail());
    }
}