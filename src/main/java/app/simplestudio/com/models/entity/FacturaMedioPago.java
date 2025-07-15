package app.simplestudio.com.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
