package snn.soluciones.com.mh;

public class ObligadoTributario {
	  public static final String TIPO_PERSONA_FISICA = "01";
	  
	  public static final String TIPO_PERSONA_JURIDICA = "02";
	  
	  private String tipoIdentificacion;
	  
	  private String numeroIdentificacion;
	  
	  public ObligadoTributario() {}
	  
	  public ObligadoTributario(String tipoIdentificacion, String numeroIdentificacion) {
	    this.tipoIdentificacion = tipoIdentificacion;
	    this.numeroIdentificacion = numeroIdentificacion;
	  }
	  
	  public String getTipoIdentificacion() {
	    return this.tipoIdentificacion;
	  }
	  
	  public void setTipoIdentificacion(String tipoIdentificacion) {
	    this.tipoIdentificacion = tipoIdentificacion;
	  }
	  
	  public String getNumeroIdentificacion() {
	    return this.numeroIdentificacion;
	  }
	  
	  public void setNumeroIdentificacion(String numeroIdentificacion) {
	    this.numeroIdentificacion = numeroIdentificacion;
	  }
	}
