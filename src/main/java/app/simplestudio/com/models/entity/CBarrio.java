package app.simplestudio.com.models.entity;

import app.simplestudio.com.models.entity.CDistrito;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "c_barrios", uniqueConstraints = {@UniqueConstraint(columnNames = {"barrio", "numero_barrio"})})
public class CBarrio {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "distrito_id")
  private CDistrito distrito;
  
  @Column(name = "numero_barrio", length = 3)
  private String numeroBarrio;
  
  @Column(length = 50)
  private String barrio;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public CDistrito getDistrito() {
    return this.distrito;
  }
  
  public void setDistrito(CDistrito distrito) {
    this.distrito = distrito;
  }
  
  public String getNumeroBarrio() {
    return this.numeroBarrio;
  }
  
  public void setNumeroBarrio(String numeroBarrio) {
    this.numeroBarrio = numeroBarrio;
  }
  
  public String getBarrio() {
    return this.barrio;
  }
  
  public void setBarrio(String barrio) {
    this.barrio = barrio;
  }
}

