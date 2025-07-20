package snn.soluciones.com;

import org.apache.xml.security.Init;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;

@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient

public class ApiFeApplication {

  private static final Logger log = LoggerFactory.getLogger(ApiFeApplication.class);

  static {
    try {
      // Registrar BouncyCastle como Security Provider
      Security.addProvider(new BouncyCastleProvider());
      log.info("BouncyCastle Security Provider registrado exitosamente");

      // Inicializar Apache XML Security
      if (!Init.isInitialized()) {
        Init.init();
        log.info("Apache XML Security inicializado exitosamente");
      }

    } catch (Exception e) {
      log.error("Error inicializando componentes de seguridad: {}", e.getMessage(), e);
      throw new RuntimeException("No se pudieron inicializar los componentes de seguridad", e);
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(ApiFeApplication.class, args);
  }

  @Bean
  public CommandLineRunner securityCheck() {
    return args -> {
      log.info("=== Verificación de Security Providers ===");
      for (java.security.Provider provider : Security.getProviders()) {
        log.info("Provider: {} - {}", provider.getName(), provider.getInfo());
      }

      // Verificar que BouncyCastle esté disponible
      if (Security.getProvider("BC") != null) {
        log.info("✓ BouncyCastle Provider está disponible");
      } else {
        log.error("✗ BouncyCastle Provider NO está disponible");
      }

      // Verificar que XML Security esté inicializado
      if (Init.isInitialized()) {
        log.info("✓ Apache XML Security está inicializado");
      } else {
        log.error("✗ Apache XML Security NO está inicializado");
      }
    };
  }
}