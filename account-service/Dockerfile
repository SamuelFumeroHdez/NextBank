# syntax=docker/dockerfile:1

# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copiamos solo el pom primero: si no cambian las dependencias, Docker reutiliza
# la capa cacheada y se salta la descarga entera en los siguientes builds.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B

# Descomponemos el JAR en las capas que define Spring Boot. Las dependencias
# (que casi nunca cambian) quedan separadas del codigo de la aplicacion
# (que cambia en cada commit), asi un despliegue solo transfiere la capa pequena.
RUN java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination extracted

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:25-jre-alpine AS runtime

# Usuario sin privilegios: si alguien compromete el proceso, no es root dentro
# del contenedor. Requisito basico en cualquier entorno regulado.
RUN addgroup -S nextbank && adduser -S nextbank -G nextbank

WORKDIR /app

COPY --from=builder --chown=nextbank:nextbank /build/extracted/dependencies/ ./
COPY --from=builder --chown=nextbank:nextbank /build/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=nextbank:nextbank /build/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=nextbank:nextbank /build/extracted/application/ ./

USER nextbank

EXPOSE 8081

# MaxRAMPercentage en lugar de -Xmx fijo: la JVM calcula el heap a partir del
# limite de memoria del contenedor, que es lo que fija Kubernetes.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8081/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
