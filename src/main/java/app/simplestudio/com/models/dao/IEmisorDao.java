package app.simplestudio.com.models.dao;

import app.simplestudio.com.models.entity.CTerminal;
import app.simplestudio.com.models.entity.Emisor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface IEmisorDao extends CrudRepository<Emisor, Long> {
  @Query("SELECT e FROM Emisor e WHERE e.identificacion = ?1 AND e.tokenAccess = ?2 AND e.statusEmpresa=true")
  Emisor findEmisorByIdentificacion(String paramString1, String paramString2);
  
  @Query("SELECT e FROM Emisor e WHERE e.identificacion = ?1")
  Emisor findEmisorOnlyIdentificacion(String paramString);

  @Query("SELECT c FROM CTerminal c INNER JOIN c.sucursal s WHERE s.emisor.id = ?1 AND s.sucursal = ?2 AND c.terminal = ?3 ORDER BY c.id DESC LIMIT 1")
  CTerminal findBySecuenciaByTerminal(Long paramLong, int paramInt1, int paramInt2);
}
