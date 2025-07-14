package app.simplestudio.com.models.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "c_provincias", uniqueConstraints = {@UniqueConstraint(columnNames = {"provincia"})})
public class CProvincia {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  private String provincia;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getProvincia() {
    return this.provincia;
  }
  
  public void setProvincia(String provincia) {
    this.provincia = provincia;
  }
}

