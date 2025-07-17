package snn.soluciones.com.models.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "emisores")
public class Emisor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tipo_de_identificacion_id")
  private CTipoDeIdentificacion tipoDeIdentificacion;
  
  @Column(unique = true)
  private String identificacion;
  
  @Column(name = "token_access")
  private String tokenAccess;
  
  @Column(length = 4)
  private String ambiente;
  
  @Column(name = "logo_empresa")
  private String logoEmpresa;
  
  @Column(name = "codigo_actividad", length = 6)
  public String codigoActividad;
  
  @Column(name = "nombre_razon_social", length = 100)
  private String nombreRazonSocial;
  
  @Column(name = "nombre_comercial", length = 80)
  private String nombreComercial;
  
  private String email;
  
  @Column(name = "codigo_pais", length = 3)
  private String codigoPais;
  
  @Column(length = 10)
  private String telefono;
  
  @Column(length = 10)
  private String fax;
  
  @Column(name = "detalle_en_factura1")
  private String detalleEnFactura1;
  
  @Column(name = "detalle_en_factura2")
  private String detalleEnFactura2;
  
  @Column(name = "nota_factura")
  private String nataFactura;
  
  private String observacion;
  
  @Column(name = "status_empresa")
  private Boolean statusEmpresa;
  
  @ManyToOne(optional = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "provincia_id")
  private CProvincia provincia;
  
  @ManyToOne(optional = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "canton_id")
  private CCanton canton;
  
  @ManyToOne(optional = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "distrito_id")
  private CDistrito distrito;
  
  @ManyToOne(optional = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "barrio_id")
  private CBarrio barrio;
  
  @Column(name = "otras_senas")
  private String otrasSenas;
  
  @Column(name = "certificado", length = 100)
  private String certificado;
  
  @Column(name = "user_api", length = 100)
  private String userApi;
  
  @Column(name = "pw_api", length = 100)
  private String pwApi;
  
  @Column(name = "ping_api", length = 4)
  private String pingApi;
  
  @Column(name = "email_notificacion", length = 100)
  private String emailNotificacion;
  
  @Column(length = 1)
  private String impresion;
  
  @Column(name = "fecha_creacion")
  @Temporal(TemporalType.DATE)
  @DateTimeFormat(pattern = "dd/MM/yyyy")
  private Date fechaCreacion;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public CTipoDeIdentificacion getTipoDeIdentificacion() {
    return this.tipoDeIdentificacion;
  }
  
  public void setTipoDeIdentificacion(CTipoDeIdentificacion tipoDeIdentificacion) {
    this.tipoDeIdentificacion = tipoDeIdentificacion;
  }
  
  public String getIdentificacion() {
    return this.identificacion;
  }
  
  public void setIdentificacion(String identificacion) {
    this.identificacion = identificacion;
  }
  
  public String getTokenAccess() {
    return this.tokenAccess;
  }
  
  public void setTokenAccess(String tokenAccess) {
    this.tokenAccess = tokenAccess;
  }
  
  public String getAmbiente() {
    return this.ambiente;
  }
  
  public void setAmbiente(String ambiente) {
    this.ambiente = ambiente;
  }
  
  public String getLogoEmpresa() {
    return this.logoEmpresa;
  }
  
  public void setLogoEmpresa(String logoEmpresa) {
    this.logoEmpresa = logoEmpresa;
  }
  
  public String getCodigoActividad() {
    return this.codigoActividad;
  }
  
  public void setCodigoActividad(String codigoActividad) {
    this.codigoActividad = codigoActividad;
  }
  
  public String getNombreRazonSocial() {
    return this.nombreRazonSocial;
  }
  
  public void setNombreRazonSocial(String nombreRazonSocial) {
    this.nombreRazonSocial = nombreRazonSocial;
  }
  
  public String getNombreComercial() {
    return this.nombreComercial;
  }
  
  public void setNombreComercial(String nombreComercial) {
    this.nombreComercial = nombreComercial;
  }
  
  public String getEmail() {
    return this.email;
  }
  
  public void setEmail(String email) {
    this.email = email;
  }
  
  public String getCodigoPais() {
    return this.codigoPais;
  }
  
  public void setCodigoPais(String codigoPais) {
    this.codigoPais = codigoPais;
  }
  
  public String getTelefono() {
    return this.telefono;
  }
  
  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }
  
  public String getFax() {
    return this.fax;
  }
  
  public void setFax(String fax) {
    this.fax = fax;
  }
  
  public String getDetalleEnFactura1() {
    return this.detalleEnFactura1;
  }
  
  public void setDetalleEnFactura1(String detalleEnFactura1) {
    this.detalleEnFactura1 = detalleEnFactura1;
  }
  
  public String getDetalleEnFactura2() {
    return this.detalleEnFactura2;
  }
  
  public void setDetalleEnFactura2(String detalleEnFactura2) {
    this.detalleEnFactura2 = detalleEnFactura2;
  }
  
  public String getNataFactura() {
    return this.nataFactura;
  }
  
  public void setNataFactura(String nataFactura) {
    this.nataFactura = nataFactura;
  }
  
  public String getObservacion() {
    return this.observacion;
  }
  
  public void setObservacion(String observacion) {
    this.observacion = observacion;
  }
  
  public Boolean getStatusEmpresa() {
    return this.statusEmpresa;
  }
  
  public void setStatusEmpresa(Boolean statusEmpresa) {
    this.statusEmpresa = statusEmpresa;
  }
  
  public CProvincia getProvincia() {
    return this.provincia;
  }
  
  public void setProvincia(CProvincia provincia) {
    this.provincia = provincia;
  }
  
  public CCanton getCanton() {
    return this.canton;
  }
  
  public void setCanton(CCanton canton) {
    this.canton = canton;
  }
  
  public CDistrito getDistrito() {
    return this.distrito;
  }
  
  public void setDistrito(CDistrito distrito) {
    this.distrito = distrito;
  }
  
  public CBarrio getBarrio() {
    return this.barrio;
  }
  
  public void setBarrio(CBarrio barrio) {
    this.barrio = barrio;
  }
  
  public String getOtrasSenas() {
    return this.otrasSenas;
  }
  
  public void setOtrasSenas(String otrasSenas) {
    this.otrasSenas = otrasSenas;
  }
  
  public String getCertificado() {
    return this.certificado;
  }
  
  public void setCertificado(String certificado) {
    this.certificado = certificado;
  }
  
  public String getUserApi() {
    return this.userApi;
  }
  
  public void setUserApi(String userApi) {
    this.userApi = userApi;
  }
  
  public String getPwApi() {
    return this.pwApi;
  }
  
  public void setPwApi(String pwApi) {
    this.pwApi = pwApi;
  }
  
  public String getPingApi() {
    return this.pingApi;
  }
  
  public void setPingApi(String pingApi) {
    this.pingApi = pingApi;
  }
  
  public String getEmailNotificacion() {
    return this.emailNotificacion;
  }
  
  public void setEmailNotificacion(String emailNotificacion) {
    this.emailNotificacion = emailNotificacion;
  }
  
  public String getImpresion() {
    return this.impresion;
  }
  
  public void setImpresion(String impresion) {
    this.impresion = impresion;
  }
  
  public Date getFechaCreacion() {
    return this.fechaCreacion;
  }
  
  public void setFechaCreacion(Date fechaCreacion) {
    this.fechaCreacion = fechaCreacion;
  }
  
  public Usuario getUsuario() {
    return this.usuario;
  }
  
  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }
}
