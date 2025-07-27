package app.simplestudio.com.models.dao;

import app.simplestudio.com.models.entity.TokenControl;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface ITokenControlDao extends CrudRepository<TokenControl, Long> {
  @Query("SELECT t FROM TokenControl t WHERE t.emisor = ?1 ORDER BY t.id DESC LIMIT 1")
  TokenControl findByEmisor(String paramString);
  
  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM TokenControl t WHERE t.emisor = ?1")
  void deleteTokenByEmisor(String paramString);
  
  @Modifying(clearAutomatically = true)
  @Query("UPDATE TokenControl t SET t.accessToken=?1, t.expiresIn=?2, t.horaCreacionToken=?3 WHERE t.id = ?4")
  void updateAccessToken(String paramString1, String paramString2, Long paramLong1, Long paramLong2);
}
