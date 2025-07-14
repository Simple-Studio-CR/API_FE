package app.simplestudio.com.service;

import app.simplestudio.com.models.dao.IFacturaDao;
import app.simplestudio.com.models.entity.Factura;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Servicio para la gestión de Factura.
 * Todas las operaciones son transaccionales.
 */
@Service
@Transactional
public class FacturaServiceImpl implements IFacturaService {
  private static final Logger log = LoggerFactory.getLogger(FacturaServiceImpl.class);

  private final IFacturaDao facturaDao;

  /**
   * Constructor con inyección de dependencias.
   *
   * @param facturaDao DAO de Factura
   */
  public FacturaServiceImpl(IFacturaDao facturaDao) {
    this.facturaDao = Objects.requireNonNull(facturaDao, "facturaDao must not be null");
    log.debug("FacturaServiceImpl initialized with DAO: {}", facturaDao.getClass().getSimpleName());
  }

  /**
   * Guarda una entidad Factura en la base de datos.
   *
   * @param entity entidad Factura a guardar
   */
  @Override
  public void save(Factura entity) {
    Objects.requireNonNull(entity, "Factura entity must not be null");
    log.debug("Saving Factura for clave='{}'", entity.getClave());
    facturaDao.save(entity);
    log.info("Factura saved for clave='{}'", entity.getClave());
  }

  /**
   * Busca una Factura por su clave única.
   *
   * @param clave clave única de la factura
   * @return objeto Factura encontrado o null si no existe
   */
  @Override
  @Transactional(readOnly = true)
  public Factura findFacturaByClave(String clave) {
    Objects.requireNonNull(clave, "clave must not be null");
    log.debug("Finding Factura with clave='{}'", clave);
    Factura factura = facturaDao.findFacturaByClave(clave);
    log.info("Factura found: {} for clave='{}'", factura, clave);
    return factura;
  }
}