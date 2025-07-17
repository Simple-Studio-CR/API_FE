package snn.soluciones.com.service.impl;

import snn.soluciones.com.models.dao.IEmisorDao;
import snn.soluciones.com.models.entity.CTerminal;
import snn.soluciones.com.models.entity.Emisor;
import snn.soluciones.com.service.IEmisorService;
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

