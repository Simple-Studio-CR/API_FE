// ==================== Enum para Tipos de Documento (Opcional) ====================
package app.simplestudio.com.enums;

public enum TipoDocumento {
    FE("Factura Electrónica"),
    TE("Tiquete Electrónico"),
    NC("Nota de Crédito"),
    ND("Nota de Débito"),
    FEC("Factura Electrónica de Compra"),
    FEE("Factura Electrónica de Exportación"),
    CCE("Confirmación Comprobante Electrónico"),
    CPCE("Confirmación Parcial Comprobante Electrónico"),
    RCE("Rechazo Comprobante Electrónico");

    private final String descripcion;

    TipoDocumento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static boolean esValido(String tipo) {
        try {
            valueOf(tipo);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}