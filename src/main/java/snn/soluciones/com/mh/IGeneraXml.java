package snn.soluciones.com.mh;

public interface IGeneraXml {
  String GeneraXml(CCampoFactura paramCCampoFactura, String paramString);
  
  String GeneraXmlDocumentos(CCampoFactura paramCCampoFactura);
  
  String GeneraXmlMr(CCampoFactura paramCCampoFactura);
  
  void generateXml(String paramString1, String paramString2, String paramString3) throws Exception;
}

