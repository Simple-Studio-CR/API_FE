package app.simplestudio.com.models.entity;

import app.simplestudio.com.models.entity.CCanton;
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
@Table(name = "c_distritos", uniqueConstraints = {@UniqueConstraint(columnNames = {"distrito", "numero_distrito"})})
public class CDistrito {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "canton_id")
  private CCanton canton;
  
  @Column(name = "numero_distrito", length = 3)
  private String numeroDistrito;
  
  @Column(length = 50)
  private String distrito;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public CCanton getCanton() {
    return this.canton;
  }
  
  public void setCanton(CCanton canton) {
    this.canton = canton;
  }
  
  public String getNumeroDistrito() {
    return this.numeroDistrito;
  }
  
  public void setNumeroDistrito(String numeroDistrito) {
    this.numeroDistrito = numeroDistrito;
  }
  
  public String getDistrito() {
    return this.distrito;
  }
  
  public void setDistrito(String distrito) {
    this.distrito = distrito;
  }
}

