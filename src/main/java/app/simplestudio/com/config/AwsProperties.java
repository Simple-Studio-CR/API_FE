package app.simplestudio.com.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "cloud.aws")
public class AwsProperties {

  private Credentials credentials;
  private S3 s3;

  @Setter
  @Getter
  public static class Credentials {

    private String accessKey;
    private String secretKey;

  }

  @Setter
  @Getter
  public static class S3 {

    private String endpoint;
    private String region;
    private String bucket;
    private String url;

  }

}