package app.simplestudio.com.mh;

import app.simplestudio.com.mh.CCampoFactura;

public interface IGeneraXml {
  String GeneraXml(CCampoFactura paramCCampoFactura);
  
  String GeneraXmlDocumentos(CCampoFactura paramCCampoFactura);
  
  String GeneraXmlMr(CCampoFactura paramCCampoFactura);
  
  void generateXml(String paramString1, String paramString2, String paramString3) throws Exception;
}

