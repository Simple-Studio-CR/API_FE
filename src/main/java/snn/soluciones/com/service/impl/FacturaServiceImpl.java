package snn.soluciones.com.service.impl;

import snn.soluciones.com.models.dao.IFacturaDao;
import snn.soluciones.com.models.entity.Factura;
import snn.soluciones.com.service.IFacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FacturaServiceImpl implements IFacturaService {
  @Autowired
  private IFacturaDao _facturaDao;
  
  public void save(Factura entity) {
    this._facturaDao.save(entity);
  }
  
  public Factura findFacturaByClave(String clave) {
    return this._facturaDao.findFacturaByClave(clave);
  }
}

