package app.simplestudio.com.controllers;

import app.simplestudio.com.models.entity.ComprobantesElectronicos;
import app.simplestudio.com.models.entity.Emisor;
import app.simplestudio.com.service.IComprobantesElectronicosService;
import app.simplestudio.com.service.IEmisorService;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperRunManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({"/api-4.3"})
public class ImpresionController {
  @Autowired
  public JavaMailSender emailSender;
  
  @Autowired
  public DataSource dataSource;
  
  @Autowired
  public IComprobantesElectronicosService _comprobantesElectronicosService;
  
  @Autowired
  public IEmisorService _emisorService;
  
  @Value("${path.upload.files.api}")
  private String pathUploadFilesApi;
  
  @Value("${url.qr}")
  private String urlQr;
  
  @GetMapping({"/imprimir-factura/{clave}"})
  @ResponseBody
  public void imprimirFactura(HttpServletResponse response, HttpSession session, @PathVariable("clave") String clave) throws JRException, IOException, SQLException {
    Connection db = this.dataSource.getConnection();
    InputStream reportfile = getClass().getResourceAsStream("/facturas.jasper");
    URL base = getClass().getResource("/");
    String baseUrl = base.toString();
    if (clave != null && clave.length() == 50) {
      ComprobantesElectronicos ce = this._comprobantesElectronicosService.findByClave(clave);
      if (ce != null) {
        Emisor e = this._emisorService.findEmisorOnlyIdentificacion(ce.getIdentificacion());
        String td = tipoDocumento(ce.getTipoDocumento());
        String logo = e.getLogoEmpresa();
        if (logo != null && !logo.equals("") && logo.length() > 0) {
          logo = this.pathUploadFilesApi + "logo/" + logo;
        } else {
          logo = this.pathUploadFilesApi + "logo/default.png";
        } 
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("BASE_URL", baseUrl);
        parameter.put("BASE_URL_LOGO", logo);
        parameter.put("CLAVE_FACTURA", clave);
        parameter.put("TIPO_DOCUMENTO", td);
        parameter.put("RESOLUCION", "“Autorizada mediante resolución Nº DGT-R-033-2019 del 20/06/2019”");
        parameter.put("NOTA_FACTURA", e.getNataFactura());
        parameter.put("URL_QR", this.urlQr + clave);
        try {
          byte[] bytes = JasperRunManager.runReportToPdf(reportfile, parameter, db);
          if (bytes != null && bytes.length > 0) {
            response.setContentType("application/pdf");
            ServletOutputStream outputstream = response.getOutputStream();
            outputstream.write(bytes, 0, bytes.length);
            outputstream.flush();
            outputstream.close();
          } else {
            System.out.println("NO trae nada");
          } 
        } catch (JRException ex) {
          System.out.println("Error del reporte: " + ex.getMessage());
        } finally {
          reportfile.close();
          try {
            if (db != null)
              db.close(); 
          } catch (SQLException ex) {
            System.out.println("Error: desconectando la base de datos.");
          } 
        } 
      } 
    } 
  }
  
  public String tipoDocumento(String td) {
    String resp = "";
    switch (td) {
      case "FE":
        resp = "Factura Electrónica";
        break;
      case "ND":
        resp = "Nota de débito Electrónica";
        break;
      case "NC":
        resp = "Nota de crédito Electrónica";
        break;
      case "TE":
        resp = "Tiquete Electrónico";
        break;
      case "FEC":
        resp = "Factura Electrónica Compra";
        break;
      case "FEE":
        resp = "Factura Electrónica Exportación";
        break;
    } 
    return resp;
  }
}

