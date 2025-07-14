package app.simplestudio.com.controllers;

import app.simplestudio.com.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String,Object>> handleNotFound(ResourceNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Map<String,Object>> handleBadRequest(BadRequestException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler({ SignException.class, XmlProcessingException.class, ExternalServiceException.class })
  public ResponseEntity<Map<String,Object>> handleConflict(ApiException ex) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String,Object>> handleOther(Exception ex) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado");
  }

  @ExceptionHandler(RecepcionNotaException.class)
  public ResponseEntity<Map<String,Object>> handleRecepcionNota(RecepcionNotaException ex) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }

  private ResponseEntity<Map<String,Object>> buildResponse(HttpStatus status, String msg) {
    Map<String,Object> body = Map.of(
        "status",   status.value(),
        "error",    status.getReasonPhrase(),
        "message",  msg,
        "timestamp", OffsetDateTime.now().toString()
    );
    return ResponseEntity.status(status).body(body);
  }
}