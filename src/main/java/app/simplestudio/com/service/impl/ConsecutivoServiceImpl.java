// ==================== Implementación de ConsecutivoService ====================
package app.simplestudio.com.service.impl;

import app.simplestudio.com.dto.FacturaRequestDTO;
import app.simplestudio.com.enums.TipoDocumento;
import app.simplestudio.com.exception.ConsecutivoGenerationException;
import app.simplestudio.com.models.entity.CTerminal;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.mh.FuncionesService;
import app.simplestudio.com.mh.Sender;
import app.simplestudio.com.service.ConsecutivoService;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ConsecutivoServiceImpl implements ConsecutivoService {

    private static final Logger log = LoggerFactory.getLogger(ConsecutivoServiceImpl.class);
    private static final int LONGITUD_CLAVE_COMPLETA = 50;
    private static final int INICIO_CONSECUTIVO = 21;
    private static final int FIN_CONSECUTIVO = 41;

    @Autowired
    private IEmisorService emisorService;

    @Autowired
    private IComprobantesElectronicosService comprobantesElectronicosService;

    @Autowired
    private FuncionesService funcionesService;

    @Autowired
    private Sender sender;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generarConsecutivo(FacturaRequestDTO request, Emisor emisor) {
        validarParametrosConsecutivo(request, emisor);

        try {
            // Si ya viene una clave completa, extraer el consecutivo
            if (esClaveCompleta(request.clave())) {
                return extraerConsecutivoDeClave(request.clave());
            }

            // Validar que existan sucursal y terminal
            validarSucursalYTerminal(request);

            // Generar nuevo consecutivo
            return generarNuevoConsecutivo(request, emisor);

        } catch (ConsecutivoGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado generando consecutivo: {}", e.getMessage(), e);
            throw new ConsecutivoGenerationException("Error inesperado generando consecutivo", e);
        }
    }

    @Override
    public String generarClave(FacturaRequestDTO request, Emisor emisor, String consecutivo) {
        validarParametrosClave(request, emisor, consecutivo);

        try {
            // Si ya viene una clave completa, validar y usar
            if (esClaveCompleta(request.clave())) {
                return request.clave();
            }

            // Generar nueva clave
            return generarNuevaClave(request, emisor, consecutivo);

        } catch (Exception e) {
            log.error("Error generando clave: {}", e.getMessage(), e);
            throw new ConsecutivoGenerationException("Error generando clave", e);
        }
    }

    // ==================== Métodos Privados ====================

    private void validarParametrosConsecutivo(FacturaRequestDTO request, Emisor emisor) {
        if (request == null) {
            throw new IllegalArgumentException("Request no puede ser null");
        }
        if (emisor == null) {
            throw new IllegalArgumentException("Emisor no puede ser null");
        }
        if (!TipoDocumento.esValido(request.tipoDocumento())) {
            throw new IllegalArgumentException("Tipo de documento no válido: " + request.tipoDocumento());
        }
    }

    private void validarParametrosClave(FacturaRequestDTO request, Emisor emisor, String consecutivo) {
        validarParametrosConsecutivo(request, emisor);
        if (!StringUtils.hasText(consecutivo)) {
            throw new IllegalArgumentException("Consecutivo no puede estar vacío");
        }
    }

    private void validarSucursalYTerminal(FacturaRequestDTO request) {
        if (!StringUtils.hasText(request.situacion())) {
            throw new IllegalArgumentException("La situación es requerida.");
        }
        if (request.sucursal() == null || request.sucursal() <= 0) {
            throw new IllegalArgumentException("La sucursal es requerida.");
        }
        if (request.terminal() == null || request.terminal() <= 0) {
            throw new IllegalArgumentException("La terminal es requerida.");
        }
    }

    private boolean esClaveCompleta(String clave) {
        return StringUtils.hasText(clave) && clave.length() == LONGITUD_CLAVE_COMPLETA;
    }

    private String extraerConsecutivoDeClave(String clave) {
        String consecutivo = clave.substring(INICIO_CONSECUTIVO, FIN_CONSECUTIVO);
        log.info("Consecutivo extraído de clave existente: {}", consecutivo);
        return consecutivo;
    }

    private String generarNuevoConsecutivo(FacturaRequestDTO request, Emisor emisor) {
        Long consecutivoEmisor = obtenerConsecutivoEmisor(request, emisor);
        Long consecutivoComprobante = obtenerConsecutivoComprobante(request, emisor);
        Long consecutivoFinal = Math.max(consecutivoEmisor, consecutivoComprobante);

        log.info("Consecutivo generado: {} (Emisor: {}, Comprobante: {})",
            consecutivoFinal, consecutivoEmisor, consecutivoComprobante);

        return String.valueOf(consecutivoFinal);
    }

    private String generarNuevaClave(FacturaRequestDTO request, Emisor emisor, String consecutivo) throws Exception {
        String tipoIdentificacion = "0" + emisor.getTipoDeIdentificacion().getId();
        String codigoSeguridad = funcionesService.getCodigoSeguridad(8);
        String sucursalPadded = funcionesService.strPad(String.valueOf(request.sucursal()), 3, "0", "strPad_LEFT");
        String terminalPadded = funcionesService.strPad(String.valueOf(request.terminal()), 5, "0", "strPad_LEFT");

        String claveResponse = sender.getClave(
            request.tipoDocumento(),
            tipoIdentificacion,
            request.emisor(),
            request.situacion(),
            emisor.getCodigoPais(),
            consecutivo,
            codigoSeguridad,
            sucursalPadded,
            terminalPadded
        );

        JsonNode claveNode = objectMapper.readTree(claveResponse);
        String claveGenerada = claveNode.path("clave").asText();

        if (!StringUtils.hasText(claveGenerada)) {
            throw new ConsecutivoGenerationException("Error: No se pudo generar la clave");
        }

        log.info("Clave generada exitosamente: {}", claveGenerada);
        return claveGenerada;
    }

    private Long obtenerConsecutivoEmisor(FacturaRequestDTO request, Emisor emisor) {
        CTerminal terminal = emisorService.findBySecuenciaByTerminal(
            emisor.getId(),
            request.sucursal(),
            request.terminal()
        );

        if (terminal == null) {
            throw new ConsecutivoGenerationException("La sucursal o la terminal no existen.");
        }

        Long consecutivo = switch (request.tipoDocumento()) {
            case "FE" -> terminal.getConsecutivoFe();
            case "TE" -> terminal.getConsecutivoTe();
            case "NC" -> terminal.getConsecutivoNc();
            case "ND" -> terminal.getConsecutivoNd();
            case "FEC" -> terminal.getConsecutivoFEC();
            case "FEE" -> terminal.getConsecutivoFEE();
            case "CCE" -> terminal.getConsecutivoCCE();
            case "CPCE" -> terminal.getConsecutivoCPCE();
            case "RCE" -> terminal.getConsecutivoRCE();
            default -> throw new ConsecutivoGenerationException("Tipo de documento no soportado: " + request.tipoDocumento());
        };

        if (consecutivo == null || consecutivo < 1) {
            log.warn("Consecutivo del emisor es null o menor a 1, usando 1 como default");
            return 1L;
        }

        return consecutivo;
    }

    private Long obtenerConsecutivoComprobante(FacturaRequestDTO request, Emisor emisor) {
        ComprobantesElectronicos ultimoComprobante = comprobantesElectronicosService.findByEmisor(
            request.emisor(),
            request.tipoDocumento().trim(),
            request.sucursal(),
            request.terminal(),
            emisor.getAmbiente()
        );

        if (ultimoComprobante != null) {
            Long siguienteConsecutivo = ultimoComprobante.getConsecutivo() + 1L;
            log.info("Consecutivo basado en último comprobante: {} + 1 = {}",
                ultimoComprobante.getConsecutivo(), siguienteConsecutivo);
            return siguienteConsecutivo;
        } else {
            log.info("No se encontraron comprobantes previos, iniciando en 1");
            return 1L;
        }
    }
}