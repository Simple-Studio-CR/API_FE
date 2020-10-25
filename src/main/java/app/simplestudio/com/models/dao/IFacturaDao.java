package app.simplestudio.com.models.dao;

import app.simplestudio.com.models.entity.Factura;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface IFacturaDao extends CrudRepository<Factura, Long> {
  @Query("SELECT c FROM Factura c WHERE c.clave = ?1")
  Factura findFacturaByClave(String paramString);
}
