// src/main/java/app/simplestudio/com/service/ConsultaDocumentoServiceImpl.java
package app.simplestudio.com.service;

import app.simplestudio.com.dto.ConsultaDocumentoRequest;
import app.simplestudio.com.dto.ConsultaDocumentoResponse;
import app.simplestudio.com.exception.RecepcionNotaException;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.mh.Sender;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultaDocumentoServiceImpl implements IConsultaDocumentoService {

    private final IEmisorService emisorService;
    private final IComprobantesElectronicosService ceService;
    private final Sender sender;

    @Value("${endpoint.prod}")
    private String endpointProd;
    @Value("${endpoint.stag}")
    private String endpointStag;
    @Value("${token.prod}")
    private String tokenProd;
    @Value("${token.stag}")
    private String tokenStag;
    @Value("${path.upload.files.api}")
    private String pathUpload;

    @Override
    public ConsultaDocumentoResponse consultarDocumento(ConsultaDocumentoRequest req)
        throws JsonProcessingException {
        Emisor e = emisorService.findEmisorByIdentificacion(req.getEmisor(), req.getTokenAccess());
        if (e == null) throw new RecepcionNotaException("Acceso denegado.");
        // escoger URL y credenciales según ambiente
        String endpoint = e.getAmbiente().equals("prod") ? endpointProd : endpointStag;
        String token   = e.getAmbiente().equals("prod") ? tokenProd  : tokenStag;
        String client  = e.getAmbiente().equals("prod") ? "api-prod"  : "api-stag";

        String raw = sender.consultarEstadoCualquierDocumento(
            endpoint,
            req.getClave(),
            e.getUserApi(),
            e.getPwApi(),
            token,
            pathUpload + "/" + e.getIdentificacion() + "/",
            client,
            e.getIdentificacion()
        );
        // raw es un JSON { clave, fecha, ind-estado, respuesta-xml }
        // parsearlo rápido con Jackson (o tu util)…
        // aquí asumo que usas Jackson directamente:
        var node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(raw);
        return ConsultaDocumentoResponse.builder()
            .response(200)
            .clave(node.path("clave").asText())
            .fecha(node.path("fecha").asText())
            .indEstado(node.path("ind-estado").asText())
            .respuestaXml(node.path("respuesta-xml").asText())
            .build();
    }

    @Override
    public ConsultaDocumentoResponse consultarDocumentoExterno(ConsultaDocumentoRequest req)
        throws JsonProcessingException {
        // misma lógica de emisor + endpoint…
        ConsultaDocumentoResponse base = consultarDocumento(req);
        // luego decodificar Base64 y extraer <MensajeHacienda>… 
        // (idéntico al viejo controller)
        String xmlB64 = base.getRespuestaXml();
        String decoded = new String(org.apache.commons.codec.binary.Base64.decodeBase64(xmlB64), java.nio.charset.StandardCharsets.UTF_8);
        // parsear DOM y extraer texto de <DetalleMensaje>…
        String mensaje;
        try {
            var dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            var db = dbf.newDocumentBuilder();
            var is = new org.xml.sax.InputSource(new java.io.StringReader(decoded));
            var doc = db.parse(is);
            var nl = doc.getElementsByTagName("DetalleMensaje");
            mensaje = org.apache.commons.text.StringEscapeUtils.escapeJava(
                nl.item(0).getTextContent()
            );
        } catch (Exception ex) {
            throw new RecepcionNotaException("Error parseando mensaje MH: " + ex.getMessage(), ex);
        }
        return ConsultaDocumentoResponse.builder()
            .response(base.getResponse())
            .clave(base.getClave())
            .fecha(base.getFecha())
            .indEstado(base.getIndEstado())
            .respuestaXml(mensaje)
            .build();
    }
}