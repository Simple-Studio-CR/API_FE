package snn.soluciones.com.service;



import snn.soluciones.com.models.entity.TokenControl;

public interface ITokenControlService {
  TokenControl findByEmisor(String paramString);
  
  void save(TokenControl paramTokenControl);
  
  void deleteTokenByEmisor(String paramString);
  
  void updateAccessToken(String paramString1, String paramString2, Long paramLong1, Long paramLong2);
}
