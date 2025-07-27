package app.simplestudio.com;

import java.util.TimeZone;
import jakarta.annotation.PostConstruct;
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
    SpringApplication.run(app.simplestudio.com.ApiMhSSApplication.class, args);
  }
}
