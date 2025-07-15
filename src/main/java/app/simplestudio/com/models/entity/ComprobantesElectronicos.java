package app.simplestudio.com.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "comprobantes_electronicos")
public class ComprobantesElectronicos {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "emisor_id")
  private Emisor emisor;
  
  private Long consecutivo;
  
  @Column(name = "tipo_documento", length = 4)
  private String tipoDocumento;
  
  @Column(length = 12)
  private String identificacion;
  
  @Column(length = 50)
  private String clave;
  
  @Column(length = 3)
  private int sucursal;
  
  @Column(length = 5)
  private int terminal;
  
  @Column(name = "fecha_emision")
  private String fechaEmision;
  
  @Column(name = "name_xml")
  private String nameXml;
  
  @Column(name = "name_xml_sign")
  private String nameXmlSign;
  
  @Column(name = "name_xml_acceptacion")
  private String nameXmlAcceptacion;
  
  @Column(name = "fecha_aceptacion")
  private String fechaAceptacion;
  
  private String ambiente;
  
  private String emailDistribucion;
  
  @Column(name = "response_code_send", length = 5)
  private String responseCodeSend;
  
  @Column(name = "ind_estado", length = 15)
  private String indEstado;
  
  @Column(columnDefinition = "TEXT")
  private String headers;
  
  @Column(length = 2, nullable = true)
  private Integer reconsultas;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public Emisor getEmisor() {
    return this.emisor;
  }
  
  public void setEmisor(Emisor emisor) {
    this.emisor = emisor;
  }
  
  public Long getConsecutivo() {
    return this.consecutivo;
  }
  
  public void setConsecutivo(Long consecutivo) {
    this.consecutivo = consecutivo;
  }
  
  public String getTipoDocumento() {
    return this.tipoDocumento;
  }
  
  public void setTipoDocumento(String tipoDocumento) {
    this.tipoDocumento = tipoDocumento;
  }
  
  public String getIdentificacion() {
    return this.identificacion;
  }
  
  public void setIdentificacion(String identificacion) {
    this.identificacion = identificacion;
  }
  
  public String getClave() {
    return this.clave;
  }
  
  public void setClave(String clave) {
    this.clave = clave;
  }
  
  public int getSucursal() {
    return this.sucursal;
  }
  
  public void setSucursal(int sucursal) {
    this.sucursal = sucursal;
  }
  
  public int getTerminal() {
    return this.terminal;
  }
  
  public void setTerminal(int terminal) {
    this.terminal = terminal;
  }
  
  public String getFechaEmision() {
    return this.fechaEmision;
  }
  
  public void setFechaEmision(String fechaEmision) {
    this.fechaEmision = fechaEmision;
  }
  
  public String getNameXml() {
    return this.nameXml;
  }
  
  public void setNameXml(String nameXml) {
    this.nameXml = nameXml;
  }
  
  public String getNameXmlSign() {
    return this.nameXmlSign;
  }
  
  public void setNameXmlSign(String nameXmlSign) {
    this.nameXmlSign = nameXmlSign;
  }
  
  public String getNameXmlAcceptacion() {
    return this.nameXmlAcceptacion;
  }
  
  public void setNameXmlAcceptacion(String nameXmlAcceptacion) {
    this.nameXmlAcceptacion = nameXmlAcceptacion;
  }
  
  public String getFechaAceptacion() {
    return this.fechaAceptacion;
  }
  
  public void setFechaAceptacion(String fechaAceptacion) {
    this.fechaAceptacion = fechaAceptacion;
  }
  
  public String getAmbiente() {
    return this.ambiente;
  }
  
  public void setAmbiente(String ambiente) {
    this.ambiente = ambiente;
  }
  
  public String getEmailDistribucion() {
    return this.emailDistribucion;
  }
  
  public void setEmailDistribucion(String emailDistribucion) {
    this.emailDistribucion = emailDistribucion;
  }
  
  public String getResponseCodeSend() {
    return this.responseCodeSend;
  }
  
  public void setResponseCodeSend(String responseCodeSend) {
    this.responseCodeSend = responseCodeSend;
  }
  
  public String getIndEstado() {
    return this.indEstado;
  }
  
  public void setIndEstado(String indEstado) {
    this.indEstado = indEstado;
  }
  
  public String getHeaders() {
    return this.headers;
  }
  
  public void setHeaders(String headers) {
    this.headers = headers;
  }
  
  public Integer getReconsultas() {
    return this.reconsultas;
  }
  
  public void setReconsultas(Integer reconsultas) {
    this.reconsultas = reconsultas;
  }
}

