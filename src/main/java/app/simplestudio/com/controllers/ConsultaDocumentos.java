package app.simplestudio.com.controllers;

import app.simplestudio.com.mh.Sender;
import app.simplestudio.com.mh.helpers.AmbienteConfigHelper;
import app.simplestudio.com.mh.helpers.EmisorValidationHelper;
import app.simplestudio.com.mh.helpers.EmisorValidationHelper.ValidacionResult;
import app.simplestudio.com.mh.helpers.XmlResponseHelper;
import app.simplestudio.com.mh.helpers.DocumentoConsultaHelper;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/api-4.3"})
@Slf4j
public class ConsultaDocumentos {

  @Autowired
  private EmisorValidationHelper emisorValidationHelper;

  @Autowired
  private Sender _sender;

  @Autowired
  private IComprobantesElectronicosService _comprobantesElectronicosService;

  @Autowired
  private XmlResponseHelper xmlResponseHelper;

  @Autowired
  private DocumentoConsultaHelper documentoConsultaHelper;

  @Value("${path.upload.files.api}")
  private String pathUploadFilesApi;

  @Value("${endpoint.prod}")
  private String endpointProd;

  @Value("${endpoint.stag}")
  private String endpointStag;

  @Value("${token.prod}")
  private String tokenProd;

  @Value("${token.stag}")
  private String tokenStag;

  @RequestMapping(value = {"/consultar-cualquier-documento"}, method = {RequestMethod.POST},
      consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> consultaCualquierDocumento(@RequestBody String j) throws Exception {
    Map<String, Object> response = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode m = objectMapper.readTree(j);

    try {
      ValidacionResult validacion = emisorValidationHelper.validarEmisor(m);
      if (!validacion.esValido()) {
        return emisorValidationHelper.respuestaAccesoDenegado();
      }

      Emisor e = validacion.emisor;
      AmbienteConfigHelper config = new AmbienteConfigHelper(e, endpointProd, endpointStag,
          tokenProd, tokenStag, pathUploadFilesApi);

      String clave = m.path("clave").asText();
      String fullPath = pathUploadFilesApi + "/" + e.getIdentificacion() + "/";

      // Consultar estado del documento
      String resturApi = _sender.consultarEstadoCualquierDocumento(
          config.endpoint, clave, config.username, config.password,
          config.urlToken, fullPath, config.clientId, e.getIdentificacion());

      JsonNode d = objectMapper.readTree(resturApi);

      // Construir respuesta usando helper
      return documentoConsultaHelper.construirRespuestaConsulta(d);

    } catch (Exception e) {
      log.error("Error consultando documento", e);
      return emisorValidationHelper.respuestaProblemasHacienda();
    }
  }

  @RequestMapping(value = {"/consultar-documentos-externos"}, method = {RequestMethod.POST},
      consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> consultaDocumentoExterno(@RequestBody String j) {
    Map<String, Object> response = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      JsonNode m = objectMapper.readTree(j);
      ValidacionResult validacion = emisorValidationHelper.validarEmisor(m);
      if (!validacion.esValido()) {
        return emisorValidationHelper.respuestaAccesoDenegado();
      }

      Emisor e = validacion.emisor;
      AmbienteConfigHelper config = new AmbienteConfigHelper(e, endpointProd, endpointStag,
          tokenProd, tokenStag, pathUploadFilesApi);

      String clave = m.path("clave").asText();
      String fullPath = pathUploadFilesApi + "/" + e.getIdentificacion() + "/";

      // Consultar estado del documento
      String resturApi = _sender.consultarEstadoCualquierDocumento(
          config.endpoint, clave, config.username, config.password,
          config.urlToken, fullPath, config.clientId, e.getIdentificacion());

      JsonNode d = objectMapper.readTree(resturApi);

      // Procesar XML de respuesta usando helper
      String mensajeMh = xmlResponseHelper.extraerMensajeHacienda(d.path("respuesta-xml").asText());

      response.put("response-code", 200);
      response.put("clave", d.path("clave").asText());
      response.put("fecha", d.path("fecha").asText());
      response.put("ind-estado", d.path("ind-estado").asText());
      response.put("respuesta-xml", mensajeMh);

      return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (Exception e) {
      log.error("Error consultando documento externo", e);
      return emisorValidationHelper.respuestaProblemasHacienda();
    }
  }

  @RequestMapping(value = {"/consultar-documentos"}, method = {RequestMethod.POST},
      consumes = {"application/json"}, produces = {"application/json"})
  public ResponseEntity<?> consultarDocumentos(@RequestBody String j) {
    Map<String, Object> response = new HashMap<>();

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode m = objectMapper.readTree(j);

      ValidacionResult validacion = emisorValidationHelper.validarEmisor(m);
      if (!validacion.esValido()) {
        return emisorValidationHelper.respuestaAccesoDenegado();
      }

      String clave = m.path("clave").asText();
      ComprobantesElectronicos ce = _comprobantesElectronicosService.findByClaveDocumento(clave);

      if (ce == null) {
        response.put("response", 404);
        response.put("msj", "Documento no encontrado");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
      }

      // Usar helper para construir respuesta
      return documentoConsultaHelper.construirRespuestaComprobante(ce);

    } catch (Exception e) {
      log.error("Error consultando documento", e);
      return emisorValidationHelper.respuestaProblemasHacienda();
    }
  }
}