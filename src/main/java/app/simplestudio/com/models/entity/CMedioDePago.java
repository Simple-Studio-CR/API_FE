package app.simplestudio.com.models.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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

