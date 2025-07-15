package app.simplestudio.com.models.entity;

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
@Table(name = "c_cantones", uniqueConstraints = {@UniqueConstraint(columnNames = {"canton", "numero_canton"})})
public class CCanton {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "provincia_id")
  private CProvincia provincia;
  
  @Column(name = "numero_canton", length = 3)
  private String numeroCanton;
  
  @Column(length = 50)
  private String canton;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public CProvincia getProvincia() {
    return this.provincia;
  }
  
  public void setProvincia(CProvincia provincia) {
    this.provincia = provincia;
  }
  
  public String getNumeroCanton() {
    return this.numeroCanton;
  }
  
  public void setNumeroCanton(String numeroCanton) {
    this.numeroCanton = numeroCanton;
  }
  
  public String getCanton() {
    return this.canton;
  }
  
  public void setCanton(String canton) {
    this.canton = canton;
  }
}

