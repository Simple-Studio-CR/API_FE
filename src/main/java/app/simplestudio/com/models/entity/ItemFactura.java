package app.simplestudio.com.models.entity;

import app.simplestudio.com.models.entity.ImpuestosItemFactura;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "facturas_items")
public class ItemFactura {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
  @JoinColumn(name = "item_factura_id")
  private List<ImpuestosItemFactura> impuestosItemFactura = new ArrayList<>();
  
  @Column(name = "numero_linea")
  private int numeroLinea;
  
  @Column(name = "partida_arancelaria", length = 15)
  private String partidaArancelaria;
  
  @Column(name = "codigo", length = 13)
  private String codigo;
  
  @Column(name = "codigo_comercial_tipo", length = 2)
  private String codigoComercialTipo;
  
  @Column(name = "codigo_comercial_codigo", length = 20)
  private String codigoComercialCodigo;
  
  @Column(name = "codigo_comercial_tipo2", length = 2)
  private String codigoComercialTipo2;
  
  @Column(name = "codigo_comercial_codigo2", length = 20)
  private String codigoComercialCodigo2;
  
  @Column(name = "codigo_comercial_tipo3", length = 2)
  private String codigoComercialTipo3;
  
  @Column(name = "codigo_comercial_codigo3", length = 20)
  private String codigoComercialCodigo3;
  
  @Column(name = "codigo_comercial_tipo4", length = 2)
  private String codigoComercialTipo4;
  
  @Column(name = "codigo_comercial_codigo4", length = 20)
  private String codigoComercialCodigo4;
  
  @Column(name = "codigo_comercial_tipo5", length = 2)
  private String codigoComercialTipo5;
  
  @Column(name = "codigo_comercial_codigo5", length = 20)
  private String codigoComercialCodigo5;
  
  @Column(precision = 18, scale = 5)
  private Double cantidad;
  
  @Column(name = "unidad_medida", length = 15)
  private String unidadMedida;
  
  @Column(name = "unidad_medida_comercial", length = 20)
  private String unidadMedidaComercial;
  
  @Column(length = 200)
  private String detalle;
  
  @Column(name = "precio_unitario", precision = 18, scale = 5)
  private Double precioUnitario;
  
  @Column(name = "monto_total", precision = 18, scale = 5)
  private Double montoTotal;
  
  @Column(name = "sub_total", precision = 18, scale = 5)
  private Double subTotal;
  
  @Column(name = "monto_descuento", precision = 18, scale = 5)
  private Double montoDescuento;
  
  @Column(name = "naturaleza_descuento")
  private String naturalezaDescuento;
  
  @Column(name = "monto_descuento2", precision = 18, scale = 5)
  private Double montoDescuento2;
  
  @Column(name = "naturaleza_descuento2")
  private String naturalezaDescuento2;
  
  @Column(name = "monto_descuento3", precision = 18, scale = 5)
  private Double montoDescuento3;
  
  @Column(name = "naturaleza_descuento3")
  private String naturalezaDescuento3;
  
  @Column(name = "monto_descuento4", precision = 18, scale = 5)
  private Double montoDescuento4;
  
  @Column(name = "naturaleza_descuento4")
  private String naturalezaDescuento4;
  
  @Column(name = "monto_descuento5", precision = 18, scale = 5)
  private Double montoDescuento5;
  
  @Column(name = "naturaleza_descuento5")
  private String naturalezaDescuento5;
  
  @Column(name = "impuesto_neto", precision = 18, scale = 5)
  private Double impuestoNeto;
  
  @Column(name = "monto_total_linea", precision = 18, scale = 5)
  private Double montoTotalLinea;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public int getNumeroLinea() {
    return this.numeroLinea;
  }
  
  public void setNumeroLinea(int numeroLinea) {
    this.numeroLinea = numeroLinea;
  }
  
  public List<ImpuestosItemFactura> getImpuestosItemFactura() {
    return this.impuestosItemFactura;
  }
  
  public void setImpuestosItemFactura(List<ImpuestosItemFactura> impuestosItemFactura) {
    this.impuestosItemFactura = impuestosItemFactura;
  }
  
  public String getPartidaArancelaria() {
    return this.partidaArancelaria;
  }
  
  public void setPartidaArancelaria(String partidaArancelaria) {
    this.partidaArancelaria = partidaArancelaria;
  }
  
  public String getCodigo() {
    return this.codigo;
  }
  
  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }
  
  public String getCodigoComercialTipo() {
    return this.codigoComercialTipo;
  }
  
  public void setCodigoComercialTipo(String codigoComercialTipo) {
    this.codigoComercialTipo = codigoComercialTipo;
  }
  
  public String getCodigoComercialCodigo() {
    return this.codigoComercialCodigo;
  }
  
  public void setCodigoComercialCodigo(String codigoComercialCodigo) {
    this.codigoComercialCodigo = codigoComercialCodigo;
  }
  
  public String getCodigoComercialTipo2() {
    return this.codigoComercialTipo2;
  }
  
  public void setCodigoComercialTipo2(String codigoComercialTipo2) {
    this.codigoComercialTipo2 = codigoComercialTipo2;
  }
  
  public String getCodigoComercialCodigo2() {
    return this.codigoComercialCodigo2;
  }
  
  public void setCodigoComercialCodigo2(String codigoComercialCodigo2) {
    this.codigoComercialCodigo2 = codigoComercialCodigo2;
  }
  
  public String getCodigoComercialTipo3() {
    return this.codigoComercialTipo3;
  }
  
  public void setCodigoComercialTipo3(String codigoComercialTipo3) {
    this.codigoComercialTipo3 = codigoComercialTipo3;
  }
  
  public String getCodigoComercialCodigo3() {
    return this.codigoComercialCodigo3;
  }
  
  public void setCodigoComercialCodigo3(String codigoComercialCodigo3) {
    this.codigoComercialCodigo3 = codigoComercialCodigo3;
  }
  
  public String getCodigoComercialTipo4() {
    return this.codigoComercialTipo4;
  }
  
  public void setCodigoComercialTipo4(String codigoComercialTipo4) {
    this.codigoComercialTipo4 = codigoComercialTipo4;
  }
  
  public String getCodigoComercialCodigo4() {
    return this.codigoComercialCodigo4;
  }
  
  public void setCodigoComercialCodigo4(String codigoComercialCodigo4) {
    this.codigoComercialCodigo4 = codigoComercialCodigo4;
  }
  
  public String getCodigoComercialTipo5() {
    return this.codigoComercialTipo5;
  }
  
  public void setCodigoComercialTipo5(String codigoComercialTipo5) {
    this.codigoComercialTipo5 = codigoComercialTipo5;
  }
  
  public String getCodigoComercialCodigo5() {
    return this.codigoComercialCodigo5;
  }
  
  public void setCodigoComercialCodigo5(String codigoComercialCodigo5) {
    this.codigoComercialCodigo5 = codigoComercialCodigo5;
  }
  
  public Double getCantidad() {
    return this.cantidad;
  }
  
  public void setCantidad(Double cantidad) {
    this.cantidad = cantidad;
  }
  
  public String getUnidadMedida() {
    return this.unidadMedida;
  }
  
  public void setUnidadMedida(String unidadMedida) {
    this.unidadMedida = unidadMedida;
  }
  
  public String getUnidadMedidaComercial() {
    return this.unidadMedidaComercial;
  }
  
  public void setUnidadMedidaComercial(String unidadMedidaComercial) {
    this.unidadMedidaComercial = unidadMedidaComercial;
  }
  
  public String getDetalle() {
    return this.detalle;
  }
  
  public void setDetalle(String detalle) {
    this.detalle = detalle;
  }
  
  public Double getPrecioUnitario() {
    return this.precioUnitario;
  }
  
  public void setPrecioUnitario(Double precioUnitario) {
    this.precioUnitario = precioUnitario;
  }
  
  public Double getMontoTotal() {
    return this.montoTotal;
  }
  
  public void setMontoTotal(Double montoTotal) {
    this.montoTotal = montoTotal;
  }
  
  public Double getSubTotal() {
    return this.subTotal;
  }
  
  public void setSubTotal(Double subTotal) {
    this.subTotal = subTotal;
  }
  
  public Double getMontoDescuento() {
    return this.montoDescuento;
  }
  
  public void setMontoDescuento(Double montoDescuento) {
    this.montoDescuento = montoDescuento;
  }
  
  public String getNaturalezaDescuento() {
    return this.naturalezaDescuento;
  }
  
  public void setNaturalezaDescuento(String naturalezaDescuento) {
    this.naturalezaDescuento = naturalezaDescuento;
  }
  
  public Double getMontoDescuento2() {
    return this.montoDescuento2;
  }
  
  public void setMontoDescuento2(Double montoDescuento2) {
    this.montoDescuento2 = montoDescuento2;
  }
  
  public String getNaturalezaDescuento2() {
    return this.naturalezaDescuento2;
  }
  
  public void setNaturalezaDescuento2(String naturalezaDescuento2) {
    this.naturalezaDescuento2 = naturalezaDescuento2;
  }
  
  public Double getMontoDescuento3() {
    return this.montoDescuento3;
  }
  
  public void setMontoDescuento3(Double montoDescuento3) {
    this.montoDescuento3 = montoDescuento3;
  }
  
  public String getNaturalezaDescuento3() {
    return this.naturalezaDescuento3;
  }
  
  public void setNaturalezaDescuento3(String naturalezaDescuento3) {
    this.naturalezaDescuento3 = naturalezaDescuento3;
  }
  
  public Double getMontoDescuento4() {
    return this.montoDescuento4;
  }
  
  public void setMontoDescuento4(Double montoDescuento4) {
    this.montoDescuento4 = montoDescuento4;
  }
  
  public String getNaturalezaDescuento4() {
    return this.naturalezaDescuento4;
  }
  
  public void setNaturalezaDescuento4(String naturalezaDescuento4) {
    this.naturalezaDescuento4 = naturalezaDescuento4;
  }
  
  public Double getMontoDescuento5() {
    return this.montoDescuento5;
  }
  
  public void setMontoDescuento5(Double montoDescuento5) {
    this.montoDescuento5 = montoDescuento5;
  }
  
  public String getNaturalezaDescuento5() {
    return this.naturalezaDescuento5;
  }
  
  public void setNaturalezaDescuento5(String naturalezaDescuento5) {
    this.naturalezaDescuento5 = naturalezaDescuento5;
  }
  
  public Double getMontoTotalLinea() {
    return this.montoTotalLinea;
  }
  
  public void setMontoTotalLinea(Double montoTotalLinea) {
    this.montoTotalLinea = montoTotalLinea;
  }
  
  public Double getImpuestoNeto() {
    return this.impuestoNeto;
  }
  
  public void setImpuestoNeto(Double impuestoNeto) {
    this.impuestoNeto = impuestoNeto;
  }
  
  public void addItemFacturaImpuestos(ImpuestosItemFactura item) {
    this.impuestosItemFactura.add(item);
  }
}
