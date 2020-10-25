package app.simplestudio.com.models.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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

