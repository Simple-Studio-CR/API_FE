package app.simplestudio.com.models.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "comprobantes_electronicos")
public class ComprobantesElectronicos {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "emisor_id")
  private Emisor emisor;

  private Long consecutivo;

  @Column(name = "tipo_documento", length = 4)
  private String tipoDocumento;

  @Column(length = 12)
  private String identificacion;

  @Column(length = 50)
  private String clave;

  @Column(length = 3)
  private int sucursal;

  @Column(length = 5)
  private int terminal;

  @Column(name = "fecha_emision")
  private String fechaEmision;

  @Column(name = "name_xml")
  private String nameXml;

  @Column(name = "name_xml_sign")
  private String nameXmlSign;

  @Column(name = "name_xml_acceptacion")
  private String nameXmlAcceptacion;

  @Column(name = "fecha_aceptacion")
  private String fechaAceptacion;

  private String ambiente;

  private String emailDistribucion;

  @Column(name = "response_code_send", length = 5)
  private String responseCodeSend;

  @Column(name = "ind_estado", length = 15)
  private String indEstado;

  @Column(columnDefinition = "TEXT")
  private String headers;

  @Column(length = 2)
  private Integer reconsultas;

}
