package app.simplestudio.com.models.dao;

import app.simplestudio.com.models.entity.MensajeReceptor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface IMensajeReceptorDao extends CrudRepository<MensajeReceptor, Long> {
  @Query("SELECT c FROM MensajeReceptor c WHERE c.clave=?1")
  MensajeReceptor findByClave(String paramString);
}
