package app.simplestudio.com.controllers;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

  @GetMapping(value = "/", produces = "application/json")
  public Map<String,String> home() {
    return Map.of(
        "status", "run",
        "response", "SNN Soluciones, 72010233 info@snnsoluciones.com."
    );
  }
}