package app.simplestudio.com.models.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
