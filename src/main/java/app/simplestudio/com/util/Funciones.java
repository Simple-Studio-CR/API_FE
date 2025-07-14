package app.simplestudio.com.util;

import app.simplestudio.com.dto.ClaveResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class Funciones {

  private final ObjectMapper mapper;

  public Funciones(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public ClaveResponse parseClaveResponse(String rawJson) {
    try {
      return mapper.readValue(rawJson, ClaveResponse.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error parseando claveResponse", e);
    }
  }

}
