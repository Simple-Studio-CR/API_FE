# Multi-stage build para optimizar el tamaño de la imagen
FROM eclipse-temurin:17-jdk-alpine AS builder

# Instalar herramientas necesarias
RUN apk add --no-cache maven

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Etapa de producción
FROM eclipse-temurin:17-jre-alpine

# Instalar dependencias del sistema para certificados digitales y fuentes
RUN apk add --no-cache \
    fontconfig \
    ttf-dejavu \
    ca-certificates \
    curl \
    && rm -rf /var/cache/apk/*

# Crear usuario no-root para seguridad
RUN addgroup -g 1001 -S spring && \
    adduser -S spring -u 1001 -G spring

# Crear directorios necesarios
RUN mkdir -p /app /home/XmlClientes /app/logs && \
    chown -R spring:spring /app /home/XmlClientes

# Cambiar al usuario no-root
USER spring

# Establecer directorio de trabajo
WORKDIR /app

# Copiar el JAR de la aplicación desde la etapa de build
COPY --from=builder --chown=spring:spring /app/target/api_fe-*.jar app.jar

# Variables de entorno por defecto (se pueden sobrescribir en DigitalOcean)
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+EnableDynamicAgentLoading"

# Configurar zona horaria
ENV TZ=America/Costa_Rica

# Exponer el puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicio con optimizaciones para contenedores
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]