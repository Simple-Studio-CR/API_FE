package app.simplestudio.com.models.dao;

import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface IComprobantesElectronicosDao extends CrudRepository<ComprobantesElectronicos, Long> {

  /**
   * Busca el comprobante más reciente (mayor consecutivo) para un emisor,
   * tipoDocumento, sucursal, terminal y ambiente dados.
   */
  @Query("SELECT c " +
      "FROM ComprobantesElectronicos c " +
      "WHERE c.identificacion = ?1 " +
      "  AND c.tipoDocumento = ?2 " +
      "  AND c.sucursal = ?3 " +
      "  AND c.terminal = ?4 " +
      "  AND c.ambiente = ?5 " +
      "ORDER BY c.consecutivo DESC")
  ComprobantesElectronicos findByEmisor(String identificacion,
      String tipoDocumento,
      int sucursal,
      int terminal,
      String ambiente);

  /**
   * Busca el comprobante con la clave dada, devolviendo el de mayor consecutivo
   * por si existieran versiones múltiples (aunque la clave es única).
   */
  @Query("SELECT c " +
      "FROM ComprobantesElectronicos c " +
      "WHERE c.clave = ?1 " +
      "ORDER BY c.consecutivo DESC")
  ComprobantesElectronicos findByClave(String clave);

  /** Alternativa para obtener el comprobante por clave sin orden adicional. */
  @Query("SELECT c FROM ComprobantesElectronicos c WHERE c.clave = ?1")
  ComprobantesElectronicos findByClaveDocumento(String clave);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE ComprobantesElectronicos c " +
      "   SET c.responseCodeSend = ?1, c.headers = ?2 " +
      " WHERE c.clave = ?3 AND c.identificacion = ?4")
  void updateComprobantesElectronicosByClaveAndEmisor(String responseCodeSend,
      String headers,
      String clave,
      String identificacion);

  @Query("SELECT c " +
      "FROM ComprobantesElectronicos c " +
      "WHERE (c.responseCodeSend IS NULL OR c.responseCodeSend = '' OR c.responseCodeSend <> '202') " +
      "  AND (c.indEstado IS NULL OR c.indEstado = '' OR c.indEstado <> 'aceptado')")
  List<ComprobantesElectronicos> findAllForSend();

  @Modifying(clearAutomatically = true)
  @Query("UPDATE ComprobantesElectronicos c " +
      "   SET c.nameXmlAcceptacion = ?1, " +
      "       c.fechaAceptacion   = ?2, " +
      "       c.indEstado         = ?3, " +
      "       c.headers           = ?4, " +
      "       c.reconsultas       = ?5 " +
      " WHERE c.clave = ?6 AND c.identificacion = ?7")
  void updateComprobantesElectronicosByClaveAndEmisor(String nameXmlAcceptacion,
      String fechaAceptacion,
      String indEstado,
      String headers,
      int reconsultas,
      String clave,
      String identificacion);

  @Query("SELECT c " +
      "FROM ComprobantesElectronicos c " +
      "JOIN FETCH c.emisor e " +
      "WHERE (c.responseCodeSend IS NOT NULL AND c.responseCodeSend <> '') " +
      "  AND (c.indEstado IS NULL OR c.indEstado = '' OR c.indEstado = 'procesando')")
  List<ComprobantesElectronicos> findAllForCheckStatus();
}