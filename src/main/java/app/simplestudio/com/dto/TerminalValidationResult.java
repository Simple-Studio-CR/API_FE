package app.simplestudio.com.dto;

import app.simplestudio.com.models.entity.CTerminal;

// TerminalValidationResult.java
public class TerminalValidationResult extends ValidationResult {
    private CTerminal terminal;

    public CTerminal getTerminal() { return terminal; }
    public void setTerminal(CTerminal terminal) { this.terminal = terminal; }
}
