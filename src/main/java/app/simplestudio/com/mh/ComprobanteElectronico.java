package app.simplestudio.com.mh;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ComprobanteElectronico {
  private String clave;
  
  private String fecha;
  
  private ObligadoTributario emisor;
  
  private ObligadoTributario receptor;
  
  private String comprobanteXml;

}

