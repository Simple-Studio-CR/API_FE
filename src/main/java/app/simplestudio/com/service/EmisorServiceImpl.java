package app.simplestudio.com.service;

import app.simplestudio.com.models.dao.IEmisorDao;
import app.simplestudio.com.models.entity.CTerminal;
import app.simplestudio.com.models.entity.Emisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmisorServiceImpl implements IEmisorService {
  @Autowired
  private IEmisorDao _emisorDao;
  
  public Emisor findEmisorByIdentificacion(String identificacion, String tokenAccess) {
    return this._emisorDao.findEmisorByIdentificacion(identificacion, tokenAccess);
  }
  
  public CTerminal findBySecuenciaByTerminal(Long emisorId, int sucursal, int terminal) {
    return this._emisorDao.findBySecuenciaByTerminal(emisorId, sucursal, terminal);
  }
  
  public Emisor findEmisorOnlyIdentificacion(String indentificacion) {
    return this._emisorDao.findEmisorOnlyIdentificacion(indentificacion);
  }
}

