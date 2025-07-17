package snn.soluciones.com.dto

;// ==================== 3. CLASES DE RESULTADO ====================

import java.util.Map;

public class ValidationResult {
    private boolean valid;
    private int httpCode;
    private Map<String, Object> errorResponse;

    // getters y setters
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public int getHttpCode() { return httpCode; }
    public void setHttpCode(int httpCode) { this.httpCode = httpCode; }
    public Map<String, Object> getErrorResponse() { return errorResponse; }
    public void setErrorResponse(Map<String, Object> errorResponse) { this.errorResponse = errorResponse; }
}