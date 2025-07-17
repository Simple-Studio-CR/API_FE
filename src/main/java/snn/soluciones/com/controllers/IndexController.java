package snn.soluciones.com.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {
  @GetMapping(value = {"/"}, produces = {"application/json"})
  public String Home() throws Exception {
    return "{\"response\":\"Samyx by SimpleStudio.app, 72010233 hello@simplestudio.app.\"}";
  }
}