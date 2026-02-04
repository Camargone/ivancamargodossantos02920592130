# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar arquivos de configuração do Maven
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build da aplicação
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Criar usuário não-root para segurança
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copiar JAR do build stage
COPY --from=build /app/target/*.jar app.jar

# Mudar ownership do arquivo
RUN chown appuser:appgroup app.jar

# Usar usuário não-root
USER appuser

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Variáveis de ambiente padrão
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Comando de execução
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
