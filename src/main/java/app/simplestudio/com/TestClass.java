package app.simplestudio.com;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class TestClass {
  public static void main(String[] args) throws Exception {
    KeyStore p12 = KeyStore.getInstance("pkcs12");
    p12.load(new FileInputStream("C:\\Users\\jmata\\Desktop\\Asada Orieta\\300230538409.p12"), "1960".toCharArray());
    Enumeration<String> e = p12.aliases();
    while (e.hasMoreElements()) {
      String alias = e.nextElement();
      X509Certificate c = (X509Certificate)p12.getCertificate(alias);
      System.out.println("Fecha de emisi" + c.getNotBefore());
      System.out.println("Fecha de caducidad: " + c.getNotAfter());
      Principal subject = c.getSubjectX500Principal();
      String[] subjectArray = subject.toString().split(",");
      for (String s : subjectArray) {
        System.out.println(s);
        String[] str = s.trim().split("=");
        String key = str[0];
        String value = str[1];
        System.out.println(key + " - " + value);
      } 
    } 
  }
}
