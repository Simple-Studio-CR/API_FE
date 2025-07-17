package snn.soluciones.com.service.impl;

import snn.soluciones.com.models.dao.ITokenControlDao;
import snn.soluciones.com.models.entity.TokenControl;
import snn.soluciones.com.service.ITokenControlService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
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
    this.tokenControlDao.updateAccessToken(accessToken, expiresIn, horaCreacionToken, idToken);
  }
}

