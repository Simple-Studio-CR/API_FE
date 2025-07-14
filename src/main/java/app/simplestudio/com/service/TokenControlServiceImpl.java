package app.simplestudio.com.service;

import app.simplestudio.com.models.dao.ITokenControlDao;
import app.simplestudio.com.models.entity.TokenControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

/**
 * Servicio para el manejo de TokenControl.
 * <p>
 * Todas las operaciones son transaccionales por defecto.
 */
@Service
@Transactional
public class TokenControlServiceImpl implements ITokenControlService {

  private static final Logger log = LoggerFactory.getLogger(TokenControlServiceImpl.class);

  private final ITokenControlDao tokenControlDao;

  /**
   * Constructor con inyección de dependencia.
   *
   * @param tokenControlDao DAO para TokenControl
   */
  public TokenControlServiceImpl(ITokenControlDao tokenControlDao) {
    this.tokenControlDao = Objects.requireNonNull(tokenControlDao, "tokenControlDao must not be null");
    log.debug("TokenControlServiceImpl initialized with DAO: {}", tokenControlDao.getClass().getSimpleName());
  }

  /**
   * Obtiene el TokenControl asociado al emisor.
   *
   * @param emisor identificador del emisor
   * @return entidad TokenControl, o null si no existe
   */
  @Override
  @Transactional(readOnly = true)
  public TokenControl findByEmisor(String emisor) {
    Objects.requireNonNull(emisor, "emisor must not be null");
    log.debug("Finding TokenControl for emisor='{}'", emisor);
    TokenControl token = tokenControlDao.findByEmisor(emisor);
    log.info("Found TokenControl: {} for emisor='{}'", token, emisor);
    return token;
  }

  /**
   * Guarda o actualiza un TokenControl.
   *
   * @param entity entidad a guardar
   */
  @Override
  public void save(TokenControl entity) {
    Objects.requireNonNull(entity, "TokenControl entity must not be null");
    log.debug("Saving TokenControl for emisor='{}'", entity.getEmisor());
    tokenControlDao.save(entity);
    log.info("TokenControl saved for emisor='{}'", entity.getEmisor());
  }

  /**
   * Elimina el TokenControl de un emisor.
   *
   * @param emisor identificador del emisor
   */
  @Override
  public void deleteTokenByEmisor(String emisor) {
    Objects.requireNonNull(emisor, "emisor must not be null");
    log.debug("Deleting TokenControl for emisor='{}'", emisor);
    tokenControlDao.deleteTokenByEmisor(emisor);
    log.info("TokenControl deleted for emisor='{}'", emisor);
  }

  /**
   * Actualiza los datos de acceso de un TokenControl existente.
   *
   * @param accessToken        nuevo token de acceso
   * @param expiresIn          tiempo de expiración
   * @param horaCreacionToken  timestamp de creación
   * @param idToken            identificador de la entidad TokenControl
   */
  @Override
  public void updateAccessToken(String accessToken,
      String expiresIn,
      Long horaCreacionToken,
      Long idToken) {
    Objects.requireNonNull(accessToken,       "accessToken must not be null");
    Objects.requireNonNull(expiresIn,         "expiresIn must not be null");
    Objects.requireNonNull(horaCreacionToken, "horaCreacionToken must not be null");
    Objects.requireNonNull(idToken,           "idToken must not be null");
    log.debug("Updating accessToken for idToken={}: expiresIn={}, horaCreacionToken={}",
        idToken, expiresIn, horaCreacionToken);
    tokenControlDao.updateAccessToken(accessToken, expiresIn, horaCreacionToken, idToken);
    log.info("AccessToken updated for idToken={}", idToken);
  }
}