package snn.soluciones.com.dto

;

import snn.soluciones.com.models.entity.Emisor;

public class EmisorValidationResult extends ValidationResult {
    private Emisor emisor;

    public Emisor getEmisor() { return emisor; }
    public void setEmisor(Emisor emisor) { this.emisor = emisor; }
}
