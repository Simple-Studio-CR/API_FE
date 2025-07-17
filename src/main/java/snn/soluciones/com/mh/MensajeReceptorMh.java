package snn.soluciones.com.mh;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MensajeReceptorMh {
  private String clave;
  
  private String fecha;
  
  private ObligadoTributario emisor;
  
  private ObligadoTributario receptor;
  
  private String comprobanteXml;
  
  private String consecutivoReceptor;

}
