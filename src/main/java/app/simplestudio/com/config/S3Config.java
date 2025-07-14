package app.simplestudio.com.config;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * Configuración para conectarse a DigitalOcean Spaces (compatible con AWS S3 SDK v2).
 */
@Configuration
public class S3Config {

  private static final Logger log = LoggerFactory.getLogger(S3Config.class);

  @Autowired
  private AwsProperties awsProperties;

  /**
   * Crea un bean de S3Client apuntando al endpoint de DigitalOcean Spaces.
   */
  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
        .region(Region.of(awsProperties.getS3().getRegion()))
        // Se utiliza el endpoint sin incluir el bucket
        .endpointOverride(URI.create(awsProperties.getS3().getEndpoint()))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(awsProperties.getCredentials().getAccessKey(),
                awsProperties.getCredentials().getSecretKey())
        ))
        .build();
  }
}