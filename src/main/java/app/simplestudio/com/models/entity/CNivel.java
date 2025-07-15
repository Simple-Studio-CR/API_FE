package app.simplestudio.com.models.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "c_niveles")
public class CNivel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "detalle_nivel")
  private String detalleNivel;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getDetalleNivel() {
    return this.detalleNivel;
  }
  
  public void setDetalleNivel(String detalleNivel) {
    this.detalleNivel = detalleNivel;
  }
}

