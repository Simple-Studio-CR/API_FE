package app.simplestudio.com.service.impl;

import app.simplestudio.com.models.dao.IComprobantesElectronicosDao;
import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComprobantesElectronicosServiceImpl implements IComprobantesElectronicosService {
  @Autowired
  private IComprobantesElectronicosDao _comprobantesElectronicosDao;
  
  @Transactional
  public void save(ComprobantesElectronicos entidad) {
    this._comprobantesElectronicosDao.save(entidad);
  }
  
  public ComprobantesElectronicos findByEmisor(String identificacion, String tipoDocumento, int sucursal, int terminal, String ambiente) {
    return this._comprobantesElectronicosDao.findByEmisor(identificacion, tipoDocumento, sucursal, terminal, ambiente);
  }
  
  public ComprobantesElectronicos findByClave(String clave) {
    return this._comprobantesElectronicosDao.findByClave(clave);
  }
  
  public ComprobantesElectronicos findByClaveDocumento(String clave) {
    return this._comprobantesElectronicosDao.findByClaveDocumento(clave);
  }
  
  @Transactional
  public void deleteById(Long id) {
    this._comprobantesElectronicosDao.deleteById(id);
  }
  
  @Transactional
  public void updateComprobantesElectronicosByClaveAndEmisor(String code, String headers, String clave, String emisor) {
    this._comprobantesElectronicosDao.updateComprobantesElectronicosByClaveAndEmisor(code, headers, clave, emisor);
  }
  
  public List<ComprobantesElectronicos> findAllForSend() {
    return this._comprobantesElectronicosDao.findAllForSend();
  }
  
  @Transactional
  public void updateComprobantesElectronicosByClaveAndEmisor(String nameXmlAcceptacion, String fechaAceptacion, String indEstado, String headers, int reconsultas, String clave, String emisor) {
    this._comprobantesElectronicosDao.updateComprobantesElectronicosByClaveAndEmisor(nameXmlAcceptacion, fechaAceptacion, indEstado, headers, reconsultas, clave, emisor);
  }
  
  public List<ComprobantesElectronicos> findAllForCheckStatus() {
    return this._comprobantesElectronicosDao.findAllForCheckStatus();
  }
}
