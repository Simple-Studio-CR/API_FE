package app.simplestudio.com.service;

import app.simplestudio.com.models.dao.IFacturaDao;
import app.simplestudio.com.models.entity.Factura;
import app.simplestudio.com.service.IFacturaService;
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

