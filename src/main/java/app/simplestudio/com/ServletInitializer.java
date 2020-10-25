package app.simplestudio.com;

import app.simplestudio.com.ApiMhSSApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(new Class[] { ApiMhSSApplication.class });
  }
}

