package app.simplestudio.com.models.entity;

import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

public class ItemSurtido {

    @Transient
    private String cabysComponente;

    @Transient
    private String descripcionComponente;

    @Transient
    private Double cantidadComponente;

    @Transient
    private Double precioUnitarioComponente;

    // --- Descuento 4.4 por componente (opcional) ---
    @Transient
    private Double montoDescuentoComponente;
    @Transient
    private String codigoDescuentoComponente;
    @Transient
    private String codigoDescuentoOtroComponente;
    @Transient
    private String naturalezaDescuentoComponente;

    // --- Impuestos del componente (opcionales) ---
    @Transient
    private List<ImpuestoComponente> impuestosComponente = new ArrayList<>();

    // Getters & Setters
    public String getCabysComponente() { return cabysComponente; }
    public void setCabysComponente(String v) { this.cabysComponente = v; }

    public String getDescripcionComponente() { return descripcionComponente; }
    public void setDescripcionComponente(String v) { this.descripcionComponente = v; }

    public Double getCantidadComponente() { return cantidadComponente; }
    public void setCantidadComponente(Double v) { this.cantidadComponente = v; }

    public Double getPrecioUnitarioComponente() { return precioUnitarioComponente; }
    public void setPrecioUnitarioComponente(Double v) { this.precioUnitarioComponente = v; }

    public Double getMontoDescuentoComponente() { return montoDescuentoComponente; }
    public void setMontoDescuentoComponente(Double v) { this.montoDescuentoComponente = v; }

    public String getCodigoDescuentoComponente() { return codigoDescuentoComponente; }
    public void setCodigoDescuentoComponente(String v) { this.codigoDescuentoComponente = v; }

    public String getCodigoDescuentoOtroComponente() { return codigoDescuentoOtroComponente; }
    public void setCodigoDescuentoOtroComponente(String v) { this.codigoDescuentoOtroComponente = v; }

    public String getNaturalezaDescuentoComponente() { return naturalezaDescuentoComponente; }
    public void setNaturalezaDescuentoComponente(String v) { this.naturalezaDescuentoComponente = v; }

    public List<ImpuestoComponente> getImpuestosComponente() { return impuestosComponente; }
    public void setImpuestosComponente(List<ImpuestoComponente> v) { this.impuestosComponente = v; }

    // --- Clase auxiliar de impuesto (POJO) ---
    public static class ImpuestoComponente {
        @Transient private String codigo;
        @Transient private String tarifa;
        @Transient private String codigoTarifa;
        @Transient private Double monto;

        public String getCodigo() { return codigo; }
        public void setCodigo(String v) { this.codigo = v; }

        public String getTarifa() { return tarifa; }
        public void setTarifa(String v) { this.tarifa = v; }

        public String getCodigoTarifa() { return codigoTarifa; }
        public void setCodigoTarifa(String v) { this.codigoTarifa = v; }

        public Double getMonto() { return monto; }
        public void setMonto(Double v) { this.monto = v; }
    }
}