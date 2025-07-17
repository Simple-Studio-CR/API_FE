package snn.soluciones.com.models.entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "c_terminales", uniqueConstraints = {@UniqueConstraint(columnNames = {"sucursal_id", "terminal"})})
public class CTerminal {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(length = 3)
  private int terminal;
  
  @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
  private CSucursal sucursal;
  
  @Column(name = "nombre_terminal", length = 200)
  private String nombreTerminal;
  
  @Column(name = "consecutivo_fe", length = 20)
  private Long consecutivoFe;
  
  @Column(name = "consecutivo_te", length = 20)
  private Long consecutivoTe;
  
  @Column(name = "consecutivo_nd", length = 20)
  private Long consecutivoNd;
  
  @Column(name = "consecutivo_nc", length = 20)
  private Long consecutivoNc;
  
  @Column(name = "consecutivo_cce", length = 20)
  private Long consecutivoCCE;
  
  @Column(name = "consecutivo_cpce", length = 20)
  private Long consecutivoCPCE;
  
  @Column(name = "consecutivo_rce", length = 20)
  private Long consecutivoRCE;
  
  @Column(name = "consecutivo_fee", length = 20)
  private Long consecutivoFEE;
  
  @Column(name = "consecutivo_fec", length = 20)
  private Long consecutivoFEC;
  
  @Column(length = 1)
  private int status;
  
  public CSucursal getSucursal() {
    return this.sucursal;
  }
  
  public void setSucursal(CSucursal sucursal) {
    this.sucursal = sucursal;
  }
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public int getTerminal() {
    return this.terminal;
  }
  
  public void setTerminal(int terminal) {
    this.terminal = terminal;
  }
  
  public String getNombreTerminal() {
    return this.nombreTerminal;
  }
  
  public void setNombreTerminal(String nombreTerminal) {
    this.nombreTerminal = nombreTerminal;
  }
  
  public Long getConsecutivoFe() {
    return this.consecutivoFe;
  }
  
  public void setConsecutivoFe(Long consecutivoFe) {
    this.consecutivoFe = consecutivoFe;
  }
  
  public Long getConsecutivoTe() {
    return this.consecutivoTe;
  }
  
  public void setConsecutivoTe(Long consecutivoTe) {
    this.consecutivoTe = consecutivoTe;
  }
  
  public Long getConsecutivoNd() {
    return this.consecutivoNd;
  }
  
  public void setConsecutivoNd(Long consecutivoNd) {
    this.consecutivoNd = consecutivoNd;
  }
  
  public Long getConsecutivoNc() {
    return this.consecutivoNc;
  }
  
  public void setConsecutivoNc(Long consecutivoNc) {
    this.consecutivoNc = consecutivoNc;
  }
  
  public Long getConsecutivoCCE() {
    return this.consecutivoCCE;
  }
  
  public void setConsecutivoCCE(Long consecutivoCCE) {
    this.consecutivoCCE = consecutivoCCE;
  }
  
  public Long getConsecutivoCPCE() {
    return this.consecutivoCPCE;
  }
  
  public void setConsecutivoCPCE(Long consecutivoCPCE) {
    this.consecutivoCPCE = consecutivoCPCE;
  }
  
  public Long getConsecutivoRCE() {
    return this.consecutivoRCE;
  }
  
  public void setConsecutivoRCE(Long consecutivoRCE) {
    this.consecutivoRCE = consecutivoRCE;
  }
  
  public Long getConsecutivoFEE() {
    return this.consecutivoFEE;
  }
  
  public void setConsecutivoFEE(Long consecutivoFEE) {
    this.consecutivoFEE = consecutivoFEE;
  }
  
  public Long getConsecutivoFEC() {
    return this.consecutivoFEC;
  }
  
  public void setConsecutivoFEC(Long consecutivoFEC) {
    this.consecutivoFEC = consecutivoFEC;
  }
  
  public int getStatus() {
    return this.status;
  }
  
  public void setStatus(int status) {
    this.status = status;
  }
}
