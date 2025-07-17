package snn.soluciones.com.models.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class Usuario implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  private String nombre;
  
  @Column(name = "apellido_p")
  private String apellidoP;
  
  @Column(name = "apellido_m")
  private String apellidoM;
  
  private String telefono;
  
  @Column(length = 100, unique = true)
  private String email;
  
  private String direccion;
  
  @Column(length = 30, unique = true)
  private String username;
  
  @Column(length = 60)
  private String password;
  
  private Boolean enabled;
  
  @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
  @JoinColumn(name = "user_id")
  private List<Role> roles = new ArrayList<>();
  
  private static final long serialVersionUID = 1L;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getNombre() {
    return this.nombre;
  }
  
  public void setNombre(String nombre) {
    this.nombre = nombre;
  }
  
  public String getApellidoP() {
    return this.apellidoP;
  }
  
  public void setApellidoP(String apellidoP) {
    this.apellidoP = apellidoP;
  }
  
  public String getApellidoM() {
    return this.apellidoM;
  }
  
  public void setApellidoM(String apellidoM) {
    this.apellidoM = apellidoM;
  }
  
  public String getTelefono() {
    return this.telefono;
  }
  
  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }
  
  public String getEmail() {
    return this.email;
  }
  
  public void setEmail(String email) {
    this.email = email;
  }
  
  public String getDireccion() {
    return this.direccion;
  }
  
  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }
  
  public String getUsername() {
    return this.username;
  }
  
  public void setUsername(String username) {
    this.username = username;
  }
  
  public String getPassword() {
    return this.password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public Boolean getEnabled() {
    return this.enabled;
  }
  
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }
  
  public List<Role> getRoles() {
    return this.roles;
  }
  
  public void setRoles(List<Role> roles) {
    this.roles = roles;
  }
  
  public void addRoleUsuario(Role rol) {
    this.roles.add(rol);
  }
}
