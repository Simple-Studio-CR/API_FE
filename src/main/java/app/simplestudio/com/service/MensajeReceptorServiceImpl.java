package app.simplestudio.com.service;

import app.simplestudio.com.models.dao.IMensajeReceptorDao;
import app.simplestudio.com.models.entity.MensajeReceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Servicio para la gestión de MensajeReceptor.
 * Todas las operaciones son transaccionales.
 */
@Service
@Transactional
public class MensajeReceptorServiceImpl implements IMensajeReceptorService {
  private static final Logger log = LoggerFactory.getLogger(MensajeReceptorServiceImpl.class);

  private final IMensajeReceptorDao mensajeReceptorDao;

  /**
   * Constructor con inyección de dependencias.
   *
   * @param mensajeReceptorDao DAO de MensajeReceptor
   */
  public MensajeReceptorServiceImpl(IMensajeReceptorDao mensajeReceptorDao) {
    this.mensajeReceptorDao = Objects.requireNonNull(mensajeReceptorDao, "mensajeReceptorDao must not be null");
    log.debug("MensajeReceptorServiceImpl initialized with DAO: {}", mensajeReceptorDao.getClass().getSimpleName());
  }

  /**
   * Guarda un MensajeReceptor en la base de datos.
   *
   * @param mr entidad MensajeReceptor a guardar
   */
  @Override
  public void save(MensajeReceptor mr) {
    Objects.requireNonNull(mr, "MensajeReceptor must not be null");
    log.debug("Saving MensajeReceptor for clave='{}'", mr.getClave());
    mensajeReceptorDao.save(mr);
    log.info("MensajeReceptor saved for clave='{}'", mr.getClave());
  }

  /**
   * Busca un MensajeReceptor por su clave.
   *
   * @param clave identificador único del mensaje
   * @return MensajeReceptor encontrado o null si no existe
   */
  @Override
  @Transactional(readOnly = true)
  public MensajeReceptor findByClave(String clave) {
    Objects.requireNonNull(clave, "clave must not be null");
    log.debug("Finding MensajeReceptor with clave='{}'", clave);
    MensajeReceptor mr = mensajeReceptorDao.findByClave(clave);
    log.info("MensajeReceptor found: {} for clave='{}'", mr, clave);
    return mr;
  }
}