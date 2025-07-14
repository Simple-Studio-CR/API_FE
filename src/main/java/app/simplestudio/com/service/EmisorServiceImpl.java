package app.simplestudio.com.service;

import app.simplestudio.com.models.dao.IEmisorDao;
import app.simplestudio.com.models.entity.CTerminal;
import app.simplestudio.com.models.entity.Emisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Servicio para la gestión de Emisor y terminales.
 * Todas las operaciones son transaccionales.
 */
@Service
@Transactional
public class EmisorServiceImpl implements IEmisorService {
  private static final Logger log = LoggerFactory.getLogger(EmisorServiceImpl.class);

  private final IEmisorDao emisorDao;

  /**
   * Constructor con inyección de dependencia.
   *
   * @param emisorDao DAO de Emisor
   */
  public EmisorServiceImpl(IEmisorDao emisorDao) {
    this.emisorDao = Objects.requireNonNull(emisorDao, "emisorDao must not be null");
    log.debug("EmisorServiceImpl initialized with DAO: {}", emisorDao.getClass().getSimpleName());
  }

  /**
   * Busca un Emisor por identificación y token de acceso.
   *
   * @param identificacion identificación del emisor
   * @param tokenAccess    token de acceso válido
   * @return Emisor encontrado o null si no existe
   */
  @Override
  @Transactional(readOnly = true)
  public Emisor findEmisorByIdentificacion(String identificacion, String tokenAccess) {
    Objects.requireNonNull(identificacion, "identificacion must not be null");
    Objects.requireNonNull(tokenAccess, "tokenAccess must not be null");
    log.debug("Finding Emisor for identificacion='{}', tokenAccess='{}'", identificacion, tokenAccess);
    Emisor emisor = emisorDao.findEmisorByIdentificacion(identificacion, tokenAccess);
    log.info("Emisor found: {} for identificacion='{}'", emisor, identificacion);
    return emisor;
  }

  /**
   * Obtiene la terminal de un emisor por secuencia de sucursal y terminal.
   *
   * @param emisorId  identificador interno del emisor
   * @param sucursal  número de sucursal
   * @param terminal  número de terminal
   * @return CTerminal encontrado o null si no existe
   */
  @Override
  @Transactional(readOnly = true)
  public CTerminal findBySecuenciaByTerminal(Long emisorId, int sucursal, int terminal) {
    Objects.requireNonNull(emisorId, "emisorId must not be null");
    log.debug("Finding CTerminal for emisorId={}, sucursal={}, terminal={}", emisorId, sucursal, terminal);
    CTerminal ct = emisorDao.findBySecuenciaByTerminal(emisorId, sucursal, terminal);
    log.info("CTerminal found: {} for emisorId={}", ct, emisorId);
    return ct;
  }

  /**
   * Busca un Emisor solo por identificación.
   *
   * @param identificacion identificación del emisor
   * @return Emisor encontrado o null si no existe
   */
  @Override
  @Transactional(readOnly = true)
  public Emisor findEmisorOnlyIdentificacion(String identificacion) {
    Objects.requireNonNull(identificacion, "identificacion must not be null");
    log.debug("Finding Emisor (only by identificacion)='{}'", identificacion);
    Emisor emisor = emisorDao.findEmisorOnlyIdentificacion(identificacion);
    log.info("Emisor found: {} for identificacion='{}'", emisor, identificacion);
    return emisor;
  }
}