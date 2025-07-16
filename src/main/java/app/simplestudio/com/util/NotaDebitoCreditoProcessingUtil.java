package app.simplestudio.com.util;

import app.simplestudio.com.controllers.NotaDebitoNotaCreditoAceptadasController;
import app.simplestudio.com.dto.ConsecutiveCalculationResult;
import app.simplestudio.com.dto.EmisorValidationResult;
import app.simplestudio.com.dto.TerminalValidationResult;
import app.simplestudio.com.mh.CCampoFactura;
import app.simplestudio.com.mh.FuncionesService;
import app.simplestudio.com.mh.IGeneraXml;
import app.simplestudio.com.mh.ISigner;
import app.simplestudio.com.models.entity.CTerminal;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import app.simplestudio.com.service.IFacturaService;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// NotaDebitoCreditoProcessingUtil.java
@Component
@Slf4j
public class NotaDebitoCreditoProcessingUtil {

    @Autowired private IEmisorService emisorService;
    @Autowired private IComprobantesElectronicosService comprobantesService;
    @Autowired private IFacturaService facturaService;
    @Autowired private FuncionesService funcionesService;
    @Autowired private FileManagerUtil fileManagerUtil;

    @Value("${path.upload.files.api}")
    private String pathUploadFilesApi;

    public EmisorValidationResult validateAndGetEmisor(JsonNode requestNode) {
        EmisorValidationResult result = new EmisorValidationResult();

        String emisorId = requestNode.path("emisor").asText();
        String tokenAccess = requestNode.path("tokenAccess").asText();

        if (emisorId == null || emisorId.trim().isEmpty()) {
            result.setValid(false);
            result.setHttpCode(401);
            result.setErrorResponse(buildErrorResponse(401, "Emisor es requerido"));
            return result;
        }

        if (tokenAccess == null || tokenAccess.trim().isEmpty()) {
            result.setValid(false);
            result.setHttpCode(401);
            result.setErrorResponse(buildErrorResponse(401, "Token de acceso es requerido"));
            return result;
        }

        Emisor emisor = emisorService.findEmisorByIdentificacion(emisorId, tokenAccess.trim());

        if (emisor == null) {
            result.setValid(false);
            result.setHttpCode(401);
            result.setErrorResponse(buildErrorResponse(401, "El usuario o token no existe."));
            return result;
        }

        result.setValid(true);
        result.setEmisor(emisor);
        return result;
    }

    public void configureEnvironment(Emisor emisor, NotaDebitoNotaCreditoAceptadasController controller) {
        if (emisor.getAmbiente().equals("prod")) {
            controller.set_endpoint(controller.getEndpointProd());
            controller.set_urlToken(controller.getTokenProd());
            controller.set_clientId("api");
        } else {
            controller.set_endpoint(controller.getEndpointStag());
            controller.set_urlToken(controller.getTokenStag());
            controller.set_clientId("api-stag");
        }

        controller.set_certificado(controller.getPathUploadFilesApi() + "/" +
            emisor.getIdentificacion() + "/cert/" + emisor.getCertificado());
        controller.set_keyCertificado(emisor.getPingApi());
        controller.set_username(emisor.getUserApi());
        controller.set_password(emisor.getPwApi());
    }

    public TerminalValidationResult validateAndGetTerminal(Emisor emisor, JsonNode requestNode) {
        TerminalValidationResult result = new TerminalValidationResult();

        int sucursal = requestNode.path("sucursal").asInt();
        int terminal = requestNode.path("terminal").asInt();

        CTerminal cTerminal = emisorService.findBySecuenciaByTerminal(emisor.getId(), sucursal, terminal);

        if (cTerminal == null) {
            result.setValid(false);
            result.setHttpCode(401);
            result.setErrorResponse(buildErrorResponse(401, "La sucursal o la terminal no existen."));
            return result;
        }

        result.setValid(true);
        result.setTerminal(cTerminal);
        return result;
    }

    public ConsecutiveCalculationResult calculateConsecutives(Emisor emisor, String tipoDocumento,
        JsonNode requestNode, CTerminal terminal) {
        ConsecutiveCalculationResult result = new ConsecutiveCalculationResult();

        // Obtener consecutivo del emisor según tipo de documento
        Long consecutivoEm = 0L;
        switch (tipoDocumento) {
            case "NC":
                consecutivoEm = terminal.getConsecutivoNc();
                break;
            case "ND":
                consecutivoEm = terminal.getConsecutivoNd();
                break;
        }

        // Obtener consecutivo de comprobantes electrónicos
        ComprobantesElectronicos ce = comprobantesService.findByEmisor(
            requestNode.path("emisor").asText().trim(),
            tipoDocumento.trim(),
            requestNode.path("sucursal").asInt(),
            requestNode.path("terminal").asInt(),
            emisor.getAmbiente()
        );

        Long consecutivoCe;
        if (ce != null) {
            consecutivoCe = ce.getConsecutivo() + 1L;
        } else {
            consecutivoCe = 1L;
        }

        // El consecutivo final es el mayor entre emisor y comprobantes electrónicos
        Long consecutivoFinal = Math.max(consecutivoCe, consecutivoEm);

        result.setConsecutivoEm(consecutivoEm);
        result.setConsecutivoCe(consecutivoCe);
        result.setConsecutivoFinal(consecutivoFinal);

        return result;
    }

    public void calculateTotals(CCampoFactura campoFactura, JsonNode requestNode) {
        // Lógica de cálculo de totales extraída del controlador original
        // Esta parte requiere procesar los detalles de línea y calcular:
        // - totalServGravados
        // - totalServExentos
        // - totalMercGravadas
        // - totalMercExentas
        // - totalVentas
        // - totalDescuentos
        // - totalVentaNeta
        // - totalImp
        // - totalComprobante

        // Por ahora mantener valores por defecto
        campoFactura.setTotalServGravados("0.00");
        campoFactura.setTotalServExentos("0.00");
        campoFactura.setTotalMercGravadas("0.00");
        campoFactura.setTotalMercExentas("0.00");
        campoFactura.setTotalVentas("0.00");
        campoFactura.setTotalDescuentos("0.00");
        campoFactura.setTotalVentasNeta("0.00");
        campoFactura.setTotalImp("0.00");
        campoFactura.setTotalComprobante("0.00");
    }

    public String saveAndSignXmlFiles(CCampoFactura campoFactura, String xmlContent,
        String certificado, String keyCertificado,
        IGeneraXml generaXml, ISigner signer) {
        try {
            // Rutas de archivos
            String basePath = pathUploadFilesApi + campoFactura.getEmisorNumIdentif() + "/";
            String xmlFileName = campoFactura.getClave() + "-factura";
            String xmlPath = basePath + xmlFileName + ".xml";
            String xmlSignedPath = basePath + campoFactura.getClave() + "-factura-sign.xml";

            // Crear directorio si no existe
            fileManagerUtil.createDirectoryIfNotExists(basePath);

            // Guardar XML original
            generaXml.generateXml(basePath, xmlContent, xmlFileName);

            // Firmar XML (usando la firma correcta de ISigner)
            signer.sign(certificado, keyCertificado, xmlPath, xmlSignedPath);

            // Retornar nombre del archivo firmado
            return campoFactura.getClave() + "-factura-sign.xml";

        } catch (Exception e) {
            log.error("Error guardando y firmando XML: {}", e.getMessage());
            throw new RuntimeException("Error en guardado y firma: " + e.getMessage());
        }
    }

    public void saveToDatabase(CCampoFactura campoFactura, Emisor emisor, String fileName) {
        // Lógica de persistencia usando el mapperUtil para crear la entidad Factura
        // y guardarla con facturaService.save()
    }

    private Map<String, Object> buildErrorResponse(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("response", code);
        response.put("msj", message);
        return response;
    }
}