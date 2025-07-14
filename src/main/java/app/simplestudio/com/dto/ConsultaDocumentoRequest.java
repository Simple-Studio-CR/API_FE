// src/main/java/app/simplestudio/com/dto/ConsultarDocumentoRequest.java
package app.simplestudio.com.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConsultaDocumentoRequest {

    @NotBlank(message = "emisor es requerido")
    private String emisor;

    @NotBlank(message = "tokenAccess es requerido")
    private String tokenAccess;

    @NotBlank(message = "clave es requerida")
    private String clave;
}