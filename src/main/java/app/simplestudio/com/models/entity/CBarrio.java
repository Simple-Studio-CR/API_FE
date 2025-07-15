package app.simplestudio.com.models.entity;

import app.simplestudio.com.models.entity.CDistrito;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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

