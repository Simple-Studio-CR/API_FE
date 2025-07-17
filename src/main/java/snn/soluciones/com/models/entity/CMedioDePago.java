package snn.soluciones.com.models.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "c_medios_de_pago")
public class CMedioDePago {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "medio_de_pago", length = 50)
  private String medioDePago;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getMedioDePago() {
    return this.medioDePago;
  }
  
  public void setMedioDePago(String medioDePago) {
    this.medioDePago = medioDePago;
  }
}

