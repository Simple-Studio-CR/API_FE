package snn.soluciones.com.dto

;

import snn.soluciones.com.models.entity.CTerminal;

// TerminalValidationResult.java
public class TerminalValidationResult extends ValidationResult {
    private CTerminal terminal;

    public CTerminal getTerminal() { return terminal; }
    public void setTerminal(CTerminal terminal) { this.terminal = terminal; }
}
