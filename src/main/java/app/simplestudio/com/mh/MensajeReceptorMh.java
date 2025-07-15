package app.simplestudio.com.mh;

public class MensajeReceptorMh {
  private String clave;
  
  private String fecha;
  
  private ObligadoTributario emisor;
  
  private ObligadoTributario receptor;
  
  private String comprobanteXml;
  
  private String consecutivoReceptor;
  
  public String getClave() {
    return this.clave;
  }
  
  public void setClave(String clave) {
    this.clave = clave;
  }
  
  public String getFecha() {
    return this.fecha;
  }
  
  public void setFecha(String fecha) {
    this.fecha = fecha;
  }
  
  public ObligadoTributario getEmisor() {
    return this.emisor;
  }
  
  public void setEmisor(ObligadoTributario emisor) {
    this.emisor = emisor;
  }
  
  public ObligadoTributario getReceptor() {
    return this.receptor;
  }
  
  public void setReceptor(ObligadoTributario receptor) {
    this.receptor = receptor;
  }
  
  public String getComprobanteXml() {
    return this.comprobanteXml;
  }
  
  public void setComprobanteXml(String comprobanteXml) {
    this.comprobanteXml = comprobanteXml;
  }
  
  public String getConsecutivoReceptor() {
    return this.consecutivoReceptor;
  }
  
  public void setConsecutivoReceptor(String consecutivoReceptor) {
    this.consecutivoReceptor = consecutivoReceptor;
  }
}
