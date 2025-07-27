package app.simplestudio.com.models.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tokens_control")
public class TokenControl {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(length = 12)
  private String emisor;
  
  @Column(name = "access_token", length = 1500)
  private String accessToken;
  
  @Column(name = "expires_in")
  private String expiresIn;
  
  @Column(name = "refresh_token", length = 1500)
  private String refreshTokens;
  
  @Column(name = "refresh_expires_in")
  private String refreshExpiresIn;
  
  @Column(name = "hora_creacion_token")
  private Long horaCreacionToken;
  
  @Column(name = "hora_creacion_refresh_token")
  private Long horaCreacionRefreshToken;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getEmisor() {
    return this.emisor;
  }
  
  public void setEmisor(String emisor) {
    this.emisor = emisor;
  }
  
  public String getAccessToken() {
    return this.accessToken;
  }
  
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
  
  public String getExpiresIn() {
    return this.expiresIn;
  }
  
  public void setExpiresIn(String expiresIn) {
    this.expiresIn = expiresIn;
  }
  
  public String getRefreshTokens() {
    return this.refreshTokens;
  }
  
  public void setRefreshTokens(String refreshTokens) {
    this.refreshTokens = refreshTokens;
  }
  
  public String getRefreshExpiresIn() {
    return this.refreshExpiresIn;
  }
  
  public void setRefreshExpiresIn(String refreshExpiresIn) {
    this.refreshExpiresIn = refreshExpiresIn;
  }
  
  public Long getHoraCreacionToken() {
    return this.horaCreacionToken;
  }
  
  public void setHoraCreacionToken(Long horaCreacionToken) {
    this.horaCreacionToken = horaCreacionToken;
  }
  
  public Long getHoraCreacionRefreshToken() {
    return this.horaCreacionRefreshToken;
  }
  
  public void setHoraCreacionRefreshToken(Long horaCreacionRefreshToken) {
    this.horaCreacionRefreshToken = horaCreacionRefreshToken;
  }
}

