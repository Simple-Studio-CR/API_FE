package app.simplestudio.com.controllers;

import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import app.simplestudio.com.util.DocumentTypeUtil;
import app.simplestudio.com.util.PrintDocumentUtil;
import app.simplestudio.com.util.ReportGeneratorUtil;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.sf.jasperreports.engine.JRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Controller
@RequestMapping({"/api-4.3"})
public class ImpresionController {

  private final Logger log = LoggerFactory.getLogger(getClass());

  // ==================== SERVICIOS ORIGINALES ====================

  @Autowired
  public DataSource dataSource;

  @Autowired
  public IComprobantesElectronicosService _comprobantesElectronicosService;

  @Autowired
  public IEmisorService _emisorService;

  // ==================== NUEVOS UTILS ====================
  @Autowired
  private DocumentTypeUtil documentTypeUtil;

  @Autowired
  private ReportGeneratorUtil reportGeneratorUtil;

  @Autowired
  private PrintDocumentUtil printDocumentUtil;

  // ==================== ENDPOINT ORIGINAL MANTENIDO ====================

  /**
   * FIRMA ORIGINAL MANTENIDA - Impresión de facturas
   */
  @GetMapping({"/imprimir-factura/{clave}"})
  @ResponseBody
  public void imprimirFactura(HttpServletResponse response, HttpSession session,
      @PathVariable("clave") String clave) throws JRException, IOException, SQLException {

    processPrintRequest(response, session, clave);
  }

  /**
   * FIRMA ORIGINAL MANTENIDA - Mapeo de tipos de documento
   */
  public String tipoDocumento(String td) {
    return documentTypeUtil.tipoDocumento(td);
  }

  // ==================== MÉTODOS AUXILIARES REFACTORIZADOS ====================

  /**
   * Procesa la solicitud completa de impresión
   */
  private void processPrintRequest(HttpServletResponse response, HttpSession session, String clave)
      throws JRException, IOException, SQLException {

    Connection dbConnection = null;

    try {
      // Logging de inicio
      printDocumentUtil.logPrintStart(clave);

      // Validaciones previas
      validatePrintRequest(clave, response);

      // Obtener datos del documento
      ComprobantesElectronicos ce = _comprobantesElectronicosService.findByClave(clave);
      if (ce == null) {
        handleDocumentNotFound(response, clave);
        return;
      }

      // Validar documento
      Map<String, Object> documentValidation = printDocumentUtil.validatePrintableDocument(ce);
      if (!(Boolean) documentValidation.get("valid")) {
        handleValidationError(response, clave, (String) documentValidation.get("error"));
        return;
      }

      // Obtener emisor
      Emisor emisor = _emisorService.findEmisorOnlyIdentificacion(ce.getIdentificacion());
      Map<String, Object> emisorValidation = printDocumentUtil.validateEmisorForPrint(emisor);
      if (!(Boolean) emisorValidation.get("valid")) {
        handleValidationError(response, clave, (String) emisorValidation.get("error"));
        return;
      }

      // Generar y escribir PDF
      generateAndWritePdf(response, ce, emisor, clave);

      // Log de éxito
      printDocumentUtil.logPrintSuccess(clave, 0); // El tamaño se logea en writePdfToResponse

    } catch (Exception e) {
      printDocumentUtil.logPrintError(clave, e.getMessage());
      handlePrintError(response, clave, e);
      throw e;

    } finally {
      cleanupPrintResources(dbConnection);
    }
  }

  /**
   * Valida la solicitud de impresión
   */
  private void validatePrintRequest(String clave, HttpServletResponse response) throws IOException {
    // Validar formato de clave
    Map<String, Object> keyValidation = printDocumentUtil.validatePrintKey(clave);
    if (!(Boolean) keyValidation.get("valid")) {
      String error = (String) keyValidation.get("error");
      printDocumentUtil.logValidationError(clave, error);
      printDocumentUtil.writeErrorToResponse(response, error);
      throw new IllegalArgumentException(error);
    }

    // Validar configuración del sistema
    Map<String, Object> systemValidation = printDocumentUtil.validateSystemConfiguration();
    if (!(Boolean) systemValidation.get("valid")) {
      String issues = (String) systemValidation.get("issues");
      log.warn("Problemas de configuración detectados: {}", issues);
      // No interrumpir por problemas menores de configuración
    }
  }

  /**
   * Genera el PDF y lo escribe al response
   */
  private void generateAndWritePdf(HttpServletResponse response, ComprobantesElectronicos ce,
      Emisor emisor, String clave) throws JRException, IOException, SQLException {

    Connection dbConnection = null;

    try {
      // Preparar conexión a base de datos
      dbConnection = dataSource.getConnection();

      // Construir parámetros para el reporte
      Map<String, Object> reportParameters = printDocumentUtil.buildPrintParameters(
          clave,
          emisor,
          ce.getTipoDocumento()
      );

      // Generar PDF usando ReportGeneratorUtil
      byte[] pdfBytes = reportGeneratorUtil.generatePdfReport(
          "/facturas.jasper",
          reportParameters,
          dbConnection
      );

      // Validar que el PDF se generó correctamente
      if (pdfBytes == null || pdfBytes.length == 0) {
        throw new JRException("El PDF generado está vacío");
      }

      // Escribir PDF al response
      printDocumentUtil.writePdfToResponse(pdfBytes, response);

      // Log detallado del documento procesado
      String documentSummary = printDocumentUtil.buildDocumentSummary(ce, emisor);
      log.info("PDF generado exitosamente para: {}", documentSummary);

    } finally {
      if (dbConnection != null) {
        try {
          dbConnection.close();
          log.debug("Conexión de base de datos cerrada");
        } catch (SQLException e) {
          log.warn("Error cerrando conexión de base de datos: {}", e.getMessage());
        }
      }
    }
  }

  /**
   * Maneja el caso cuando no se encuentra el documento
   */
  private void handleDocumentNotFound(HttpServletResponse response, String clave) throws IOException {
    String errorMessage = "Documento no encontrado para clave: " + clave;
    printDocumentUtil.logValidationError(clave, errorMessage);
    printDocumentUtil.writeErrorToResponse(response, errorMessage);
  }

  /**
   * Maneja errores de validación
   */
  private void handleValidationError(HttpServletResponse response, String clave, String error) throws IOException {
    printDocumentUtil.logValidationError(clave, error);
    printDocumentUtil.writeErrorToResponse(response, error);
  }

  /**
   * Maneja errores generales de impresión
   */
  private void handlePrintError(HttpServletResponse response, String clave, Exception e) {
    try {
      String errorMessage = "Error generando PDF: " + e.getMessage();

      // Diferenciar tipos de errores
      if (e instanceof JRException) {
        errorMessage = "Error en reporte JasperReports: " + e.getMessage();
      } else if (e instanceof SQLException) {
        errorMessage = "Error de base de datos: " + e.getMessage();
      } else if (e instanceof IOException) {
        errorMessage = "Error de E/O: " + e.getMessage();
      }

      printDocumentUtil.writeErrorToResponse(response, errorMessage);

    } catch (IOException ioException) {
      log.error("Error adicional escribiendo respuesta de error: {}", ioException.getMessage());
    }
  }

  /**
   * Limpia recursos utilizados en la impresión
   */
  private void cleanupPrintResources(Connection dbConnection) {
    if (dbConnection != null) {
      try {
        dbConnection.close();
        log.debug("Recursos de impresión limpiados correctamente");
      } catch (SQLException e) {
        log.error("Error limpiando recursos de impresión: {}", e.getMessage());
      }
    }
  }

}