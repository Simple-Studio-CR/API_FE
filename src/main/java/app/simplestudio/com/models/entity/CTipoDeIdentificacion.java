package app.simplestudio.com.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "c_tipo_de_identificacion")
public class CTipoDeIdentificacion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "tipo_de_identificacion")
  private String tipoDeIdentificacion;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getTipoDeIdentificacion() {
    return this.tipoDeIdentificacion;
  }
  
  public void setTipoDeIdentificacion(String tipoDeIdentificacion) {
    this.tipoDeIdentificacion = tipoDeIdentificacion;
  }
}
