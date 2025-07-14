// ==================== Configuration Bean ====================
package app.simplestudio.com.config;

import app.simplestudio.com.service.generator.XmlGeneratorService;
import app.simplestudio.com.service.generator.impl.S3OnlyXmlGeneratorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuración para usar S3OnlyXmlGeneratorService como primary
 */
@Configuration
public class S3ServiceConfiguration {

    /**
     * Hacer que S3OnlyXmlGeneratorService sea el generador principal
     */
    @Bean
    @Primary
    public XmlGeneratorService xmlGeneratorService(S3OnlyXmlGeneratorService s3Generator) {
        return s3Generator;
    }
}