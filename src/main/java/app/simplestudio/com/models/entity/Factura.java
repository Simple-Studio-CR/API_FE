package app.simplestudio.com.models.entity;

import app.simplestudio.com.models.entity.FacturaOtrosCargos;
import app.simplestudio.com.models.entity.FacturaReferencia;
import app.simplestudio.com.models.entity.ItemFactura;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "facturas")
public class Factura {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "numero_factura", length = 20)
  private String numeroFactura;
  
  @Column(name = "tipo_documento", length = 4)
  private String tipoDocumento;
  
  @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
  @JoinColumn(name = "factura_id")
  private List<ItemFactura> items = new ArrayList<>();
  
  @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
  @JoinColumn(name = "factura_id")
  private List<FacturaReferencia> itemsReferencias = new ArrayList<>();
  
  @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true)
  @JoinColumn(name = "factura_id")
  private List<FacturaOtrosCargos> itemsOtrosCargos = new ArrayList<>();
  
  @Column(length = 50)
  private String clave;
  
  @Column(length = 20)
  private String consecutivo;
  
  @Column(name = "codigoActividad", length = 6)
  private String codigoActividad;
  
  @Column(name = "fecha_emision", length = 40)
  private String fechaEmision;
  
  @Column(name = "emisor_nombre", length = 100)
  public String emisorNombre;
  
  @Column(name = "emisor_tipo_identif", length = 2)
  public String emisorTipoIdentif;
  
  @Column(name = "emisor_num_identif", length = 12)
  public String emisorNumIdentif;
  
  @Column(name = "nombre_comercial", length = 80)
  public String nombreComercial;
  
  @Column(name = "emisor_prov", length = 1)
  public String emisorProv;
  
  @Column(name = "emisor_canton", length = 2)
  public String emisorCanton;
  
  @Column(name = "emisor_distrito", length = 2)
  public String emisorDistrito;
  
  @Column(name = "emisor_barrio", length = 2)
  public String emisorBarrio;
  
  @Column(name = "emisor_otras_senas", length = 250)
  public String emisorOtrasSenas;
  
  @Column(name = "emisor_cod_pais_tel", length = 3)
  public String emisorCodPaisTel;
  
  @Column(name = "emisor_tel", length = 20)
  public String emisorTel;
  
  @Column(name = "emisor_cod_pais_fax", length = 3)
  public String emisorCodPaisFax;
  
  @Column(name = "emisor_fax", length = 20)
  public String emisorFax;
  
  @Column(name = "emisor_email", length = 160)
  public String emisorEmail;
  
  @Column(length = 12)
  private String identificacion;
  
  @Column(length = 13)
  private String situacion;
  
  @Column(length = 5)
  private String sucursal;
  
  @Column(length = 5)
  private String terminal;
  
  @Column(name = "omitir_receptor", length = 5)
  private String omitirReceptor;
  
  @Column(name = "receptor_nombre")
  private String receptorNombre;
  
  @Column(name = "receptor_tipo_identif", length = 2)
  private String receptorTipoIdentif;
  
  @Column(name = "receptorNumIdentif", length = 20)
  private String receptor_num_identif;
  
  @Column(name = "receptor_identificacion_extranjero", length = 20)
  private String receptorIdentificacionExtranjero;
  
  @Column(name = "receptor_provincia", length = 1)
  private String receptorProvincia;
  
  @Column(name = "receptor_canton", length = 2)
  private String receptorCanton;
  
  @Column(name = "receptor_distrito", length = 2)
  private String receptorDistrito;
  
  @Column(name = "receptor_barrio", length = 2)
  private String receptorBarrio;
  
  @Column(name = "receptor_otras_senas", length = 160)
  private String receptorOtrasSenas;
  
  @Column(name = "otras_senas_extranjero", length = 300)
  private String otrasSenasExtranjero;
  
  @Column(name = "receptor_cod_pais_tel", length = 5)
  private String receptorCodPaisTel;
  
  @Column(name = "receptor_tel", length = 10)
  private String receptorTel;
  
  @Column(name = "receptor_cod_pais_fax", length = 5)
  private String receptorCodPaisFax;
  
  @Column(name = "receptor_fax", length = 10)
  private String receptorFax;
  
  @Column(name = "receptor_email", length = 160)
  private String receptorEmail;
  
  @Column(name = "cond_venta", length = 2)
  private String condVenta;
  
  @Column(name = "plazoCredito", length = 10)
  private String plazoCredito;
  
  @Column(name = "medio_pago", length = 2)
  private String medioPago;
  
  @Column(name = "medio_pago_2", length = 2)
  private String medioPago2;
  
  @Column(name = "medio_pago_3", length = 2)
  private String medioPago3;
  
  @Column(name = "medio_pago_4", length = 2)
  private String medioPago4;
  
  @Column(name = "cod_moneda", length = 5)
  private String codMoneda;
  
  @Column(name = "tipoCambio", length = 8)
  private String tipoCambio;
  
  @Column(name = "total_serv_gravados", length = 20)
  private String totalServGravados;
  
  @Column(name = "total_serv_exentos", length = 20)
  private String totalServExentos;
  
  @Column(name = "total_serv_exonerado", length = 20)
  private String totalServExonerado;
  
  @Column(name = "total_merc_gravadas", length = 20)
  private String totalMercGravadas;
  
  @Column(name = "total_merc_exentas", length = 20)
  private String totalMercExentas;
  
  @Column(name = "total_merc_exonerada", length = 20)
  private String totalMercExonerada;
  
  @Column(name = "total_gravados", length = 20)
  private String totalGravados;
  
  @Column(name = "total_exentos", length = 20)
  private String totalExentos;
  
  @Column(name = "total_exonerado", length = 20)
  private String totalExonerado;
  
  @Column(name = "total_ventas", length = 20)
  private String totalVentas;
  
  @Column(name = "total_descuentos", length = 20)
  private String totalDescuentos;
  
  @Column(name = "total_venta_neta", length = 20)
  private String totalVentaNeta;
  
  @Column(name = "total_imp", length = 20)
  private String totalImp;
  
  @Column(name = "total_iva_devuelto", length = 20)
  private String totalIVADevuelto;
  
  @Column(name = "total_otros_cargos", length = 20)
  private String totalOtrosCargos;
  
  @Column(name = "total_comprobante", length = 20)
  private String totalComprobante;
  
  @Column(name = "otros")
  private String otros;
  
  public Long getId() {
    return this.id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public String getNumeroFactura() {
    return this.numeroFactura;
  }
  
  public void setNumeroFactura(String numeroFactura) {
    this.numeroFactura = numeroFactura;
  }
  
  public List<ItemFactura> getItems() {
    return this.items;
  }
  
  public void setItems(List<ItemFactura> items) {
    this.items = items;
  }
  
  public List<FacturaReferencia> getItemsReferencias() {
    return this.itemsReferencias;
  }
  
  public void setItemsReferencias(List<FacturaReferencia> itemsReferencias) {
    this.itemsReferencias = itemsReferencias;
  }
  
  public String getClave() {
    return this.clave;
  }
  
  public void setClave(String clave) {
    this.clave = clave;
  }
  
  public String getCodigoActividad() {
    return this.codigoActividad;
  }
  
  public void setCodigoActividad(String codigoActividad) {
    this.codigoActividad = codigoActividad;
  }
  
  public String getConsecutivo() {
    return this.consecutivo;
  }
  
  public void setConsecutivo(String consecutivo) {
    this.consecutivo = consecutivo;
  }
  
  public String getFechaEmision() {
    return this.fechaEmision;
  }
  
  public void setFechaEmision(String fechaEmision) {
    this.fechaEmision = fechaEmision;
  }
  
  public String getEmisorNombre() {
    return this.emisorNombre;
  }
  
  public void setEmisorNombre(String emisorNombre) {
    this.emisorNombre = emisorNombre;
  }
  
  public String getEmisorTipoIdentif() {
    return this.emisorTipoIdentif;
  }
  
  public void setEmisorTipoIdentif(String emisorTipoIdentif) {
    this.emisorTipoIdentif = emisorTipoIdentif;
  }
  
  public String getEmisorNumIdentif() {
    return this.emisorNumIdentif;
  }
  
  public void setEmisorNumIdentif(String emisorNumIdentif) {
    this.emisorNumIdentif = emisorNumIdentif;
  }
  
  public String getNombreComercial() {
    return this.nombreComercial;
  }
  
  public void setNombreComercial(String nombreComercial) {
    this.nombreComercial = nombreComercial;
  }
  
  public String getEmisorProv() {
    return this.emisorProv;
  }
  
  public void setEmisorProv(String emisorProv) {
    this.emisorProv = emisorProv;
  }
  
  public String getEmisorCanton() {
    return this.emisorCanton;
  }
  
  public void setEmisorCanton(String emisorCanton) {
    this.emisorCanton = emisorCanton;
  }
  
  public String getEmisorDistrito() {
    return this.emisorDistrito;
  }
  
  public void setEmisorDistrito(String emisorDistrito) {
    this.emisorDistrito = emisorDistrito;
  }
  
  public String getEmisorBarrio() {
    return this.emisorBarrio;
  }
  
  public void setEmisorBarrio(String emisorBarrio) {
    this.emisorBarrio = emisorBarrio;
  }
  
  public String getEmisorOtrasSenas() {
    return this.emisorOtrasSenas;
  }
  
  public void setEmisorOtrasSenas(String emisorOtrasSenas) {
    this.emisorOtrasSenas = emisorOtrasSenas;
  }
  
  public String getEmisorCodPaisTel() {
    return this.emisorCodPaisTel;
  }
  
  public void setEmisorCodPaisTel(String emisorCodPaisTel) {
    this.emisorCodPaisTel = emisorCodPaisTel;
  }
  
  public String getEmisorTel() {
    return this.emisorTel;
  }
  
  public void setEmisorTel(String emisorTel) {
    this.emisorTel = emisorTel;
  }
  
  public String getEmisorCodPaisFax() {
    return this.emisorCodPaisFax;
  }
  
  public void setEmisorCodPaisFax(String emisorCodPaisFax) {
    this.emisorCodPaisFax = emisorCodPaisFax;
  }
  
  public String getEmisorFax() {
    return this.emisorFax;
  }
  
  public void setEmisorFax(String emisorFax) {
    this.emisorFax = emisorFax;
  }
  
  public String getEmisorEmail() {
    return this.emisorEmail;
  }
  
  public void setEmisorEmail(String emisorEmail) {
    this.emisorEmail = emisorEmail;
  }
  
  public String getIdentificacion() {
    return this.identificacion;
  }
  
  public void setIdentificacion(String identificacion) {
    this.identificacion = identificacion;
  }
  
  public String getSituacion() {
    return this.situacion;
  }
  
  public void setSituacion(String situacion) {
    this.situacion = situacion;
  }
  
  public String getTerminal() {
    return this.terminal;
  }
  
  public void setTerminal(String terminal) {
    this.terminal = terminal;
  }
  
  public String getSucursal() {
    return this.sucursal;
  }
  
  public void setSucursal(String sucursal) {
    this.sucursal = sucursal;
  }
  
  public String getOmitirReceptor() {
    return this.omitirReceptor;
  }
  
  public void setOmitirReceptor(String omitirReceptor) {
    this.omitirReceptor = omitirReceptor;
  }
  
  public String getReceptorNombre() {
    return this.receptorNombre;
  }
  
  public void setReceptorNombre(String receptorNombre) {
    this.receptorNombre = receptorNombre;
  }
  
  public String getReceptorTipoIdentif() {
    return this.receptorTipoIdentif;
  }
  
  public void setReceptorTipoIdentif(String receptorTipoIdentif) {
    this.receptorTipoIdentif = receptorTipoIdentif;
  }
  
  public String getReceptor_num_identif() {
    return this.receptor_num_identif;
  }
  
  public void setReceptor_num_identif(String receptor_num_identif) {
    this.receptor_num_identif = receptor_num_identif;
  }
  
  public String getReceptorProvincia() {
    return this.receptorProvincia;
  }
  
  public void setReceptorProvincia(String receptorProvincia) {
    this.receptorProvincia = receptorProvincia;
  }
  
  public String getReceptorCanton() {
    return this.receptorCanton;
  }
  
  public void setReceptorCanton(String receptorCanton) {
    this.receptorCanton = receptorCanton;
  }
  
  public String getReceptorDistrito() {
    return this.receptorDistrito;
  }
  
  public void setReceptorDistrito(String receptorDistrito) {
    this.receptorDistrito = receptorDistrito;
  }
  
  public String getReceptorBarrio() {
    return this.receptorBarrio;
  }
  
  public void setReceptorBarrio(String receptorBarrio) {
    this.receptorBarrio = receptorBarrio;
  }
  
  public String getReceptorOtrasSenas() {
    return this.receptorOtrasSenas;
  }
  
  public void setReceptorOtrasSenas(String receptorOtrasSenas) {
    this.receptorOtrasSenas = receptorOtrasSenas;
  }
  
  public String getReceptorCodPaisTel() {
    return this.receptorCodPaisTel;
  }
  
  public void setReceptorCodPaisTel(String receptorCodPaisTel) {
    this.receptorCodPaisTel = receptorCodPaisTel;
  }
  
  public String getReceptorTel() {
    return this.receptorTel;
  }
  
  public void setReceptorTel(String receptorTel) {
    this.receptorTel = receptorTel;
  }
  
  public String getReceptorCodPaisFax() {
    return this.receptorCodPaisFax;
  }
  
  public void setReceptorCodPaisFax(String receptorCodPaisFax) {
    this.receptorCodPaisFax = receptorCodPaisFax;
  }
  
  public String getReceptorFax() {
    return this.receptorFax;
  }
  
  public void setReceptorFax(String receptorFax) {
    this.receptorFax = receptorFax;
  }
  
  public String getReceptorEmail() {
    return this.receptorEmail;
  }
  
  public void setReceptorEmail(String receptorEmail) {
    this.receptorEmail = receptorEmail;
  }
  
  public String getCondVenta() {
    return this.condVenta;
  }
  
  public void setCondVenta(String condVenta) {
    this.condVenta = condVenta;
  }
  
  public String getPlazoCredito() {
    return this.plazoCredito;
  }
  
  public void setPlazoCredito(String plazoCredito) {
    this.plazoCredito = plazoCredito;
  }
  
  public String getMedioPago() {
    return this.medioPago;
  }
  
  public void setMedioPago(String medioPago) {
    this.medioPago = medioPago;
  }
  
  public String getCodMoneda() {
    return this.codMoneda;
  }
  
  public void setCodMoneda(String codMoneda) {
    this.codMoneda = codMoneda;
  }
  
  public String getTipoCambio() {
    return this.tipoCambio;
  }
  
  public void setTipoCambio(String tipoCambio) {
    this.tipoCambio = tipoCambio;
  }
  
  public String getTotalServGravados() {
    return this.totalServGravados;
  }
  
  public void setTotalServGravados(String totalServGravados) {
    this.totalServGravados = totalServGravados;
  }
  
  public String getTotalServExentos() {
    return this.totalServExentos;
  }
  
  public void setTotalServExentos(String totalServExentos) {
    this.totalServExentos = totalServExentos;
  }
  
  public String getTotalMercGravadas() {
    return this.totalMercGravadas;
  }
  
  public void setTotalMercGravadas(String totalMercGravadas) {
    this.totalMercGravadas = totalMercGravadas;
  }
  
  public String getTotalMercExentas() {
    return this.totalMercExentas;
  }
  
  public void setTotalMercExentas(String totalMercExentas) {
    this.totalMercExentas = totalMercExentas;
  }
  
  public String getTotalGravados() {
    return this.totalGravados;
  }
  
  public void setTotalGravados(String totalGravados) {
    this.totalGravados = totalGravados;
  }
  
  public String getTotalExentos() {
    return this.totalExentos;
  }
  
  public void setTotalExentos(String totalExentos) {
    this.totalExentos = totalExentos;
  }
  
  public String getTotalVentas() {
    return this.totalVentas;
  }
  
  public void setTotalVentas(String totalVentas) {
    this.totalVentas = totalVentas;
  }
  
  public String getTotalDescuentos() {
    return this.totalDescuentos;
  }
  
  public void setTotalDescuentos(String totalDescuentos) {
    this.totalDescuentos = totalDescuentos;
  }
  
  public String getTotalVentaNeta() {
    return this.totalVentaNeta;
  }
  
  public void setTotalVentaNeta(String totalVentaNeta) {
    this.totalVentaNeta = totalVentaNeta;
  }
  
  public String getTotalImp() {
    return this.totalImp;
  }
  
  public void setTotalImp(String totalImp) {
    this.totalImp = totalImp;
  }
  
  public String getTotalComprobante() {
    return this.totalComprobante;
  }
  
  public void setTotalComprobante(String totalComprobante) {
    this.totalComprobante = totalComprobante;
  }
  
  public String getOtros() {
    return this.otros;
  }
  
  public void setOtros(String otros) {
    this.otros = otros;
  }
  
  public String getTipoDocumento() {
    return this.tipoDocumento;
  }
  
  public void setTipoDocumento(String tipoDocumento) {
    this.tipoDocumento = tipoDocumento;
  }
  
  public String getMedioPago2() {
    return this.medioPago2;
  }
  
  public void setMedioPago2(String medioPago2) {
    this.medioPago2 = medioPago2;
  }
  
  public String getMedioPago3() {
    return this.medioPago3;
  }
  
  public void setMedioPago3(String medioPago3) {
    this.medioPago3 = medioPago3;
  }
  
  public String getMedioPago4() {
    return this.medioPago4;
  }
  
  public void setMedioPago4(String medioPago4) {
    this.medioPago4 = medioPago4;
  }
  
  public String getTotalServExonerado() {
    return this.totalServExonerado;
  }
  
  public void setTotalServExonerado(String totalServExonerado) {
    this.totalServExonerado = totalServExonerado;
  }
  
  public String getTotalMercExonerada() {
    return this.totalMercExonerada;
  }
  
  public void setTotalMercExonerada(String totalMercExonerada) {
    this.totalMercExonerada = totalMercExonerada;
  }
  
  public String getTotalExonerado() {
    return this.totalExonerado;
  }
  
  public void setTotalExonerado(String totalExonerado) {
    this.totalExonerado = totalExonerado;
  }
  
  public String getTotalIVADevuelto() {
    return this.totalIVADevuelto;
  }
  
  public void setTotalIVADevuelto(String totalIVADevuelto) {
    this.totalIVADevuelto = totalIVADevuelto;
  }
  
  public String getTotalOtrosCargos() {
    return this.totalOtrosCargos;
  }
  
  public void setTotalOtrosCargos(String totalOtrosCargos) {
    this.totalOtrosCargos = totalOtrosCargos;
  }
  
  public String getReceptorIdentificacionExtranjero() {
    return this.receptorIdentificacionExtranjero;
  }
  
  public void setReceptorIdentificacionExtranjero(String receptorIdentificacionExtranjero) {
    this.receptorIdentificacionExtranjero = receptorIdentificacionExtranjero;
  }
  
  public String getOtrasSenasExtranjero() {
    return this.otrasSenasExtranjero;
  }
  
  public void setOtrasSenasExtranjero(String otrasSenasExtranjero) {
    this.otrasSenasExtranjero = otrasSenasExtranjero;
  }
  
  public void addItemFactura(ItemFactura item) {
    this.items.add(item);
  }
  
  public void addReferenciaFactura(FacturaReferencia referencia) {
    this.itemsReferencias.add(referencia);
  }
  
  public void addOtrosCargos(FacturaOtrosCargos otrosCargos) {
    this.itemsOtrosCargos.add(otrosCargos);
  }
}

