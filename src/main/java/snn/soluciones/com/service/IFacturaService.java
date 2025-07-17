package snn.soluciones.com.service;


import snn.soluciones.com.models.entity.Factura;

public interface IFacturaService {
  void save(Factura paramFactura);
  
  Factura findFacturaByClave(String paramString);
}

