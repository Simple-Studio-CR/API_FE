package app.simplestudio.com.service;


import app.simplestudio.com.models.entity.MensajeReceptor;

public interface IMensajeReceptorService {
  void save(MensajeReceptor paramMensajeReceptor);
  
  MensajeReceptor findByClave(String paramString);
}

