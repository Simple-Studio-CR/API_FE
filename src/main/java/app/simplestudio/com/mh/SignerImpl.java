package app.simplestudio.com.mh;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xades4j.production.Enveloped;
import xades4j.production.XadesEpesSigningProfile;
import xades4j.production.XadesSigner;
import xades4j.properties.ObjectIdentifier;
import xades4j.properties.SignaturePolicyIdentifierProperty;
import xades4j.providers.SignaturePolicyInfoProvider;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.impl.FileSystemKeyStoreKeyingDataProvider;

/**
 * Servicio encargado de firmar documentos XML según el estándar XAdES EPES.
 * <p>
 * Pasos principales:
 * <ol>
 *   <li>Configurar política de firma.</li>
 *   <li>Cargar el almacén de claves PKCS#12.</li>
 *   <li>Firmar el XML (enveloped signature).</li>
 *   <li>Volcar el documento firmado a disco.</li>
 * </ol>
 */
@Service
public class SignerImpl implements ISigner {
  private static final Logger log = LoggerFactory.getLogger(SignerImpl.class);

  /** URL pública de la política de firma Hacienda v4.3 **/
  private static final String POLICY_URL =
      "https://www.hacienda.go.cr/ATV/ComprobanteElectronico/docs/esquemas/2016/"
          + "v4.3/Resolución_General_sobre_disposiciones_técnicas_comprobantes_electrónicos_para_efectos_tributarios.pdf";

  /** Descripción de la política para incluir en la firma **/
  private static final byte[] POLICY_DESCRIPTION =
      "Política de Factura Digital".getBytes();

  /**
   * Firma un documento XML con XAdES EPES.
   *
   * @param keyStorePath        Ruta al almacén PKCS#12.
   * @param keyStorePassword    Contraseña del almacén.
   * @param xmlInputPath        Ruta del XML original.
   * @param xmlOutputPath       Ruta donde se escribirá el XML firmado.
   */
  @Override
  public void sign(Path keyStorePath,
      String keyStorePassword,
      Path xmlInputPath,
      Path xmlOutputPath) {

    // Validar parámetros de entrada
    Objects.requireNonNull(keyStorePath,     "keyStorePath must not be null");
    Objects.requireNonNull(keyStorePassword, "keyStorePassword must not be null");
    Objects.requireNonNull(xmlInputPath,     "xmlInputPath must not be null");
    Objects.requireNonNull(xmlOutputPath,    "xmlOutputPath must not be null");

    log.info("Iniciando proceso de firma para: {}", xmlInputPath);

    try {
      // 1) Configurar proveedor de política de firma
      log.info("Configurando política de firma XAdES desde URL: {}", POLICY_URL);
      SignaturePolicyInfoProvider policyProvider = () -> {
        ObjectIdentifier policyId = new ObjectIdentifier(POLICY_URL, null);
        return new SignaturePolicyIdentifierProperty(
            policyId,
            new ByteArrayInputStream(POLICY_DESCRIPTION)
        );
      };

      // 2) Cargar el KeyStore PKCS#12 con las credenciales proporcionadas
      log.info("Cargando almacén de claves PKCS#12 desde: {}", keyStorePath);
      KeyingDataProvider keyProvider = new FileSystemKeyStoreKeyingDataProvider(
          "pkcs12",
          keyStorePath.toString(),
          new FirstCertificateSelector(),
          new DirectPasswordProvider(keyStorePassword),
          new DirectPasswordProvider(keyStorePassword),
          false
      );

      // 3) Crear perfil y firmador XAdES
      log.info("Inicializando XAdES EPES Signing Profile");
      XadesEpesSigningProfile signingProfile =
          new XadesEpesSigningProfile(keyProvider, policyProvider);
      XadesSigner signer = signingProfile.newSigner();

      // 4) Parsear el documento XML de entrada
      log.info("Parseando XML de entrada");
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      dbFactory.setNamespaceAware(true);
      Document document;
      try (var is = Files.newInputStream(xmlInputPath)) {
        document = dbFactory.newDocumentBuilder().parse(is);
      }

      // 5) Aplicar firma enveloped
      log.info("Firmando documento (enveloped signature)");
      Element rootElement = document.getDocumentElement();
      new Enveloped(signer).sign(rootElement);

      // 6) Escribir el documento firmado en disco
      log.info("Volcando XML firmado a: {}", xmlOutputPath);
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      Source source = new DOMSource(document);
      try (var os = Files.newOutputStream(xmlOutputPath)) {
        Result result = new StreamResult(os);
        transformer.transform(source, result);
      }

      log.info("Firma completada exitosamente para: {}", xmlOutputPath);
    } catch (Exception e) {
      log.error("Error al firmar documento XML: {}", e.getMessage(), e);
      throw new RuntimeException("Firma XAdES fallida", e);
    }
  }
}