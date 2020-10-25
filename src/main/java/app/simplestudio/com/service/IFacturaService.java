package app.simplestudio.com.service;

import app.simplestudio.com.models.entity.Factura;

public interface IFacturaService {
  void save(Factura paramFactura);
  
  Factura findFacturaByClave(String paramString);
}

