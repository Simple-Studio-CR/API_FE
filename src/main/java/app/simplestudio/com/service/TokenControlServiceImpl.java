package app.simplestudio.com.service;

import app.simplestudio.com.models.dao.ITokenControlDao;
import app.simplestudio.com.models.entity.TokenControl;
import app.simplestudio.com.service.ITokenControlService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TokenControlServiceImpl implements ITokenControlService {
  @Autowired
  private ITokenControlDao tokenControlDao;
  
  public TokenControl findByEmisor(String emisor) {
    return this.tokenControlDao.findByEmisor(emisor);
  }
  
  @Transactional
  public void save(TokenControl entity) {
    this.tokenControlDao.save(entity);
  }
  
  @Transactional
  public void deleteTokenByEmisor(String emisor) {
    this.tokenControlDao.deleteTokenByEmisor(emisor);
  }
  
  @Transactional
  public void updateAccessToken(String accessToken, String expiresIn, Long horaCreacionToken, Long idToken) {
    if (accessToken == null || accessToken.isEmpty()) {
      log.error("Intento de actualizar con accessToken nulo o vacío");
      return;
    }

    if (expiresIn == null || expiresIn.isEmpty()) {
      expiresIn = "3600"; // Valor por defecto
    }
    this.tokenControlDao.updateAccessToken(accessToken, expiresIn, horaCreacionToken, idToken);
  }
}

