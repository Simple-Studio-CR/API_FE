package app.simplestudio.com.models.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "facturas_referencias")
public class FacturaReferencia {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(length = 2)
  private String tipoDoc;
  
  @Column(length = 50)
  private String numero;
  
  @Column(length = 25)
  private String fechaEmision;
  
  @Column(length = 2)
  private String codigo;
  
  @Column(length = 180)
  private String razon;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getTipoDoc() {
    return this.tipoDoc;
  }
  
  public void setTipoDoc(String tipoDoc) {
    this.tipoDoc = tipoDoc;
  }
  
  public String getNumero() {
    return this.numero;
  }
  
  public void setNumero(String numero) {
    this.numero = numero;
  }
  
  public String getFechaEmision() {
    return this.fechaEmision;
  }
  
  public void setFechaEmision(String fechaEmision) {
    this.fechaEmision = fechaEmision;
  }
  
  public String getCodigo() {
    return this.codigo;
  }
  
  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }
  
  public String getRazon() {
    return this.razon;
  }
  
  public void setRazon(String razon) {
    this.razon = razon;
  }
}

