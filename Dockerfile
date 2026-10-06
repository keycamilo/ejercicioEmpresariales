# =========================================================
# ETAPA 1: Compilación (Build Stage)
# =========================================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copia dependencias y código fuente
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# =========================================================
# ETAPA 2: Ejecución (Runtime Stage)
# =========================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia el ejecutable principal generado (excluyendo el .original)
COPY --from=builder /app/target/*.jar app.jar

# Expone el puerto por defecto
EXPOSE 8088

# Optimización de la JVM para evitar consumo excesivo de memoria
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -Xss512k"

ENTRYPOINT ["java", "-jar", "app.jar"]