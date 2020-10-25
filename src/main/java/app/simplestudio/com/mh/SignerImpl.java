package app.simplestudio.com.mh;

import java.io.ByteArrayInputStream;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import xades4j.production.Enveloped;
import xades4j.production.XadesEpesSigningProfile;
import xades4j.production.XadesSigner;
import xades4j.properties.ObjectIdentifier;
import xades4j.properties.SignaturePolicyBase;
import xades4j.properties.SignaturePolicyIdentifierProperty;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.SignaturePolicyInfoProvider;
import xades4j.providers.impl.FileSystemKeyStoreKeyingDataProvider;

@Service
public class SignerImpl implements ISigner {
  public void sign(String keyPath, String password, String xmlInPath, String xmlOutPath) {
    DocumentBuilderFactory factory = null;
    DocumentBuilder builder = null;
    Document doc1 = null;
    Element elemToSign = null;
    XadesSigner signer = null;
    Transformer transformer = null;
    Result output = null;
    Source input = null;
    try {
      SignaturePolicyInfoProvider policyInfoProvider = new SignaturePolicyInfoProvider() {
          public SignaturePolicyBase getSignaturePolicy() {
            return (SignaturePolicyBase)new SignaturePolicyIdentifierProperty(new ObjectIdentifier("https://www.hacienda.go.cr/ATV/ComprobanteElectronico/docs/esquemas/2016/v4.3/Resolució_General_sobre_disposiciones_ténicas_comprobantes_electróicos_para_efectos_tributarios.pdf"), new ByteArrayInputStream("Politica de Factura Digital"
                  
                  .getBytes()));
          }
        };
      FileSystemKeyStoreKeyingDataProvider fileSystemKeyStoreKeyingDataProvider = new FileSystemKeyStoreKeyingDataProvider("pkcs12", keyPath, new FirstCertificateSelector(), new DirectPasswordProvider(password), new DirectPasswordProvider(password), false);
      XadesEpesSigningProfile xadesEpesSigningProfile = new XadesEpesSigningProfile((KeyingDataProvider)fileSystemKeyStoreKeyingDataProvider, policyInfoProvider);
      factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      builder = factory.newDocumentBuilder();
      doc1 = builder.parse(new File(xmlInPath));
      elemToSign = doc1.getDocumentElement();
      signer = xadesEpesSigningProfile.newSigner();
      (new Enveloped(signer)).sign(elemToSign);
      transformer = TransformerFactory.newInstance().newTransformer();
      output = new StreamResult(xmlOutPath);
      input = new DOMSource(doc1);
      transformer.transform(input, output);
      System.out.println("Documento firmado con éito");
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      builder = null;
      doc1 = null;
      elemToSign = null;
      signer = null;
      transformer = null;
      output = null;
      input = null;
    } 
  }
}


