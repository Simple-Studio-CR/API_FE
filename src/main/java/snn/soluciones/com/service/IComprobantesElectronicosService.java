package snn.soluciones.com.service;

import snn.soluciones.com.models.entity.ComprobantesElectronicos;
import java.util.List;

public interface IComprobantesElectronicosService {
  void save(ComprobantesElectronicos paramComprobantesElectronicos);
  
  ComprobantesElectronicos findByEmisor(String paramString1, String paramString2, int paramInt1, int paramInt2, String paramString3);
  
  ComprobantesElectronicos findByClave(String paramString);
  
  ComprobantesElectronicos findByClaveDocumento(String paramString);
  
  void deleteById(Long paramLong);
  
  void updateComprobantesElectronicosByClaveAndEmisor(String paramString1, String paramString2, String paramString3, String paramString4);
  
  List<ComprobantesElectronicos> findAllForSend();
  
  void updateComprobantesElectronicosByClaveAndEmisor(String paramString1, String paramString2, String paramString3, String paramString4, int paramInt, String paramString5, String paramString6);
  
  List<ComprobantesElectronicos> findAllForCheckStatus();
}

