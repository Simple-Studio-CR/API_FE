package app.simplestudio.com.models.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "factura_medio_pago")
public class FacturaMedioPago {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "medigo_pago", length = 2)
  private String medioPago;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getMedioPago() {
    return this.medioPago;
  }
  
  public void setMedioPago(String medioPago) {
    this.medioPago = medioPago;
  }
}
