package app.simplestudio.com.util;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.NumberFormat;

@Component
public class XmlValidationUtil {
    
    /**
     * Aplica padding a string según las reglas de MH
     * MANTIENE LA MISMA LÓGICA que FuncionesService.str_pad
     */
    public String str_pad(String input, int padLength, String padString, String padType) {
        if (input == null) {
            input = "";
        }
        
        if (input.length() >= padLength) {
            return input;
        }
        
        int padNeeded = padLength - input.length();
        StringBuilder padding = new StringBuilder();
        
        // Crear el padding necesario
        while (padding.length() < padNeeded) {
            padding.append(padString);
        }
        
        // Truncar si es necesario
        if (padding.length() > padNeeded) {
            padding.setLength(padNeeded);
        }
        
        // Aplicar padding según el tipo
        if ("STR_PAD_LEFT".equals(padType)) {
            return padding.toString() + input;
        } else {
            return input + padding.toString();
        }
    }
    
    /**
     * Procesa texto para XML con escape de caracteres
     * MANTIENE LA MISMA LÓGICA que procesarTexto()
     */
    public String procesarTexto(String input) {
        if (input == null) {
            return "";
        }
        return StringEscapeUtils.escapeXml10(input);
    }
    
    /**
     * Procesa números con formato decimal específico
     * MANTIENE LA MISMA LÓGICA que procesarNumeros()
     */
    public String procesarNumeros(String input, String decimales) {
        NumberFormat formatter = new DecimalFormat(decimales);
        String result = (input != null && !input.trim().isEmpty()) ? input : "0.00";
        
        try {
            result = formatter.format(Double.parseDouble(result));
            result = result.replaceAll(",", ".");
        } catch (NumberFormatException e) {
            result = "0.00";
        }
        
        return result;
    }
    
    /**
     * Valida y formatea código de identificación con padding
     */
    public String formatearTipoIdentificacion(String tipo) {
        return str_pad(tipo, 2, "0", "STR_PAD_LEFT");
    }
    
    /**
     * Valida y formatea código de actividad con padding
     */
    public String formatearCodigoActividad(String codigo) {
        return str_pad(codigo, 6, "0", "STR_PAD_LEFT");
    }
    
    /**
     * Valida y formatea códigos de ubicación (cantón, distrito, barrio)
     */
    public String formatearCodigoUbicacion(String codigo) {
        return str_pad(codigo, 2, "0", "STR_PAD_LEFT");
    }
    
    /**
     * Valida y formatea condition de venta
     */
    public String formatearCondicionVenta(String condicion) {
        return str_pad(condicion, 2, "0", "STR_PAD_LEFT");
    }
    
    /**
     * Valida y formatea medio de pago
     */
    public String formatearMedioPago(String medio) {
        return str_pad(medio, 2, "0", "STR_PAD_LEFT");
    }
    
    /**
     * Valida y formatea código de impuesto
     */
    public String formatearCodigoImpuesto(String codigo) {
        return str_pad(codigo, 2, "0", "STR_PAD_LEFT");
    }
    
    /**
     * Valida y formatea código de tarifa
     */
    public String formatearCodigoTarifa(String tarifa) {
        return str_pad(tarifa, 2, "0", "STR_PAD_LEFT");
    }
    
    /**
     * Valida y formatea tipo de documento
     */
    public String formatearTipoDocumento(String tipo) {
        return str_pad(tipo, 2, "0", "STR_PAD_LEFT");
    }
    
    /**
     * Valida y formatea código comercial
     */
    public String formatearCodigoComercial(String codigo) {
        return str_pad(codigo, 2, "0", "STR_PAD_LEFT");
    }
    
    /**
     * Valida y formatea número de cédula para Mensaje Receptor
     */
    public String formatearNumeroCedula(String cedula) {
        return str_pad(cedula, 12, "0", "STR_PAD_LEFT");
    }
    
    /**
     * Valida si un string no es nulo ni vacío
     */
    public boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Valida si un string no es nulo ni vacío y no es "0"
     */
    public boolean isValidValue(String value) {
        return isNotEmpty(value) && !"0".equals(value.trim());
    }
    
    /**
     * Valida si un double es mayor a 0
     */
    public boolean isPositiveAmount(String amount) {
        try {
            return Double.parseDouble(amount) > 0.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Valida formato de email básico
     */
    public boolean isValidEmail(String email) {
        return isNotEmpty(email) && email.contains("@");
    }
    
    /**
     * Valida si es identificación extranjera (tipo 05)
     */
    public boolean isIdentificacionExtranjera(String tipoIdentificacion) {
        return "05".equals(tipoIdentificacion) || "5".equals(tipoIdentificacion);
    }
    
    /**
     * Valida si el tipo de documento requiere ciertos campos específicos
     */
    public boolean requiresExoneracion(String tipoDocumento) {
        return !"09".equals(tipoDocumento);
    }
    
    /**
     * Valida si el tipo de documento requiere campos de exportación
     */
    public boolean isFacturaExportacion(String tipoDocumento) {
        return "09".equals(tipoDocumento);
    }
    
    /**
     * Valida si el tipo de documento requiere IVA devuelto
     */
    public boolean requiresIVADevuelto(String tipoDocumento) {
        return !"08".equals(tipoDocumento) && !"09".equals(tipoDocumento);
    }
    
    /**
     * Valida si el tipo de documento es factura estándar
     */
    public boolean isFacturaEstandar(String tipoDocumento) {
        return "01".equals(tipoDocumento) || "07".equals(tipoDocumento) || "04".equals(tipoDocumento);
    }
    
    /**
     * Formatea montos monetarios con 2 decimales
     */
    public String formatearMonto(String monto) {
        return procesarNumeros(monto, "#0.00");
    }
    
    /**
     * Formatea porcentajes con 2 decimales
     */
    public String formatearPorcentaje(String porcentaje) {
        return procesarNumeros(porcentaje, "#0.00");
    }
}