package app.simplestudio.com.models.dao;

import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface IComprobantesElectronicosDao extends CrudRepository<ComprobantesElectronicos, Long> {
  @Query("SELECT MAX(c) FROM ComprobantesElectronicos c WHERE c.identificacion=?1 AND c.tipoDocumento = ?2 AND c.sucursal = ?3 AND c.terminal=?4 AND c.ambiente = ?5")
  ComprobantesElectronicos findByEmisor(String paramString1, String paramString2, int paramInt1, int paramInt2, String paramString3);
  
  @Query("SELECT MAX(c) FROM ComprobantesElectronicos c WHERE c.clave=?1")
  ComprobantesElectronicos findByClave(String paramString);
  
  @Query("SELECT c FROM ComprobantesElectronicos c WHERE c.clave=?1")
  ComprobantesElectronicos findByClaveDocumento(String paramString);
  
  @Modifying(clearAutomatically = true)
  @Query("UPDATE ComprobantesElectronicos c SET c.responseCodeSend=?1, c.headers = ?2 WHERE c.clave =?3 AND c.identificacion = ?4")
  void updateComprobantesElectronicosByClaveAndEmisor(String paramString1, String paramString2, String paramString3, String paramString4);
  
  @Query("SELECT c FROM ComprobantesElectronicos c WHERE c.responseCodeSend IS NULL OR c.responseCodeSend = '' OR c.responseCodeSend != '202' AND (c.indEstado != 'aceptado' OR c.indEstado IS NULL OR c.indEstado = '')")
  List<ComprobantesElectronicos> findAllForSend();
  
  @Modifying(clearAutomatically = true)
  @Query("UPDATE ComprobantesElectronicos c SET c.nameXmlAcceptacion=?1, c.fechaAceptacion=?2, c.indEstado=?3, c.headers=?4, c.reconsultas=?5 WHERE c.clave=?6 AND c.identificacion = ?7")
  void updateComprobantesElectronicosByClaveAndEmisor(String paramString1, String paramString2, String paramString3, String paramString4, int paramInt, String paramString5, String paramString6);
  
  @Query("SELECT c FROM ComprobantesElectronicos c JOIN FETCH c.emisor e WHERE (c.responseCodeSend != null OR c.responseCodeSend != '') AND (c.indEstado IS NULL OR c.indEstado = '' OR c.indEstado = 'procesando')")
  List<ComprobantesElectronicos> findAllForCheckStatus();
}
