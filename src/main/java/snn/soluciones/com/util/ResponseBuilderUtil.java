
// ========================================
// 5. ResponseBuilderUtil.java
// ========================================
package snn.soluciones.com.util;


import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class ResponseBuilderUtil {
    
    public Map<String, Object> buildErrorResponse(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("response", code);
        response.put("msj", message);
        return response;
    }
    
    public Map<String, Object> buildSuccessResponse(String clave, String consecutivo, 
                                                   String fechaEmision, String fileName) {
        Map<String, Object> response = new HashMap<>();
        response.put("response", 200);
        response.put("clave", clave);
        response.put("consecutivo", consecutivo);
        response.put("fechaEmision", fechaEmision);
        response.put("fileXmlSign", fileName);
        return response;
    }
}