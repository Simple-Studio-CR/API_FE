package snn.soluciones.com;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiMhSSApplication {
  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Costa_Rica"));
  }
  
  public static void main(String[] args) {
    SpringApplication.run(ApiMhSSApplication.class, args);
  }
}
