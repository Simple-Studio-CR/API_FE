package snn.soluciones.com.service.impl;

import snn.soluciones.com.models.dao.IMensajeReceptorDao;
import snn.soluciones.com.models.entity.MensajeReceptor;
import snn.soluciones.com.service.IMensajeReceptorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MensajeReceptorServiceImpl implements IMensajeReceptorService {
  @Autowired
  private IMensajeReceptorDao _mensajeReceptorDao;
  
  public void save(MensajeReceptor mr) {
    this._mensajeReceptorDao.save(mr);
  }
  
  public MensajeReceptor findByClave(String clave) {
    return this._mensajeReceptorDao.findByClave(clave);
  }
}

