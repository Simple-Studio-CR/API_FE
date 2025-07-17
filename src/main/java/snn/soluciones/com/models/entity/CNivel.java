package snn.soluciones.com.models.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

