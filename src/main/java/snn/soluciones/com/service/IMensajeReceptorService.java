package snn.soluciones.com.service;



import snn.soluciones.com.models.entity.MensajeReceptor;

public interface IMensajeReceptorService {
  void save(MensajeReceptor paramMensajeReceptor);
  
  MensajeReceptor findByClave(String paramString);
}

