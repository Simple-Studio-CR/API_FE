package snn.soluciones.com.service;


import snn.soluciones.com.models.entity.CTerminal;
import snn.soluciones.com.models.entity.Emisor;

public interface IEmisorService {
  Emisor findEmisorByIdentificacion(String paramString1, String paramString2);
  
  CTerminal findBySecuenciaByTerminal(Long paramLong, int paramInt1, int paramInt2);
  
  Emisor findEmisorOnlyIdentificacion(String paramString);
}
