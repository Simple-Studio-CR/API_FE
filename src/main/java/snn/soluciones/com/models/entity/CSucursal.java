package snn.soluciones.com.models.entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "c_sucursales", uniqueConstraints = {@UniqueConstraint(columnNames = {"emisor_id", "sucursal"})})
public class CSucursal {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(length = 3)
  private int sucursal;
  
  @ManyToOne(fetch = FetchType.LAZY)
  private Emisor emisor;
  
  @Column(name = "nombre_sucursal", length = 200)
  private String nombreSucursal;
  
  @Column(length = 8)
  private String telefono;
  
  @Column(name = "correo_sucursal", length = 100)
  private String correoSucursal;
  
  private String direccion;
  
  @Column(name = "encargado_sucursal", length = 150)
  private String encargadoSucursal;
  
  @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
  @JoinColumn(name = "user_id")
  private Usuario usuario;
  
  @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
  @JoinColumn(name = "sucursal_id")
  private List<CTerminal> terminales = new ArrayList<>();
  
  @Column(length = 1)
  private int status;
  
  public List<CTerminal> getTerminales() {
    return this.terminales;
  }
  
  public void setTerminales(List<CTerminal> terminales) {
    this.terminales = terminales;
  }
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public int getSucursal() {
    return this.sucursal;
  }
  
  public void setSucursal(int sucursal) {
    this.sucursal = sucursal;
  }
  
  public Emisor getEmisor() {
    return this.emisor;
  }
  
  public void setEmisor(Emisor emisor) {
    this.emisor = emisor;
  }
  
  public String getNombreSucursal() {
    return this.nombreSucursal;
  }
  
  public void setNombreSucursal(String nombreSucursal) {
    this.nombreSucursal = nombreSucursal;
  }
  
  public String getTelefono() {
    return this.telefono;
  }
  
  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }
  
  public String getDireccion() {
    return this.direccion;
  }
  
  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }
  
  public String getEncargadoSucursal() {
    return this.encargadoSucursal;
  }
  
  public void setEncargadoSucursal(String encargadoSucursal) {
    this.encargadoSucursal = encargadoSucursal;
  }
  
  public String getCorreoSucursal() {
    return this.correoSucursal;
  }
  
  public void setCorreoSucursal(String correoSucursal) {
    this.correoSucursal = correoSucursal;
  }
  
  public Usuario getUsuario() {
    return this.usuario;
  }
  
  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }
  
  public void addSucursalEmisor(CTerminal cs) {
    this.terminales.add(cs);
  }
  
  public int getStatus() {
    return this.status;
  }
  
  public void setStatus(int status) {
    this.status = status;
  }
}

