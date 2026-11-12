# ---------- Build ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Dependencias en una capa propia: solo se re-descargan si cambia pom.xml
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package \
    && mv target/*.jar app.jar \
    && java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# ---------- Runtime ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# TZ / user.timezone: las fechas (incidencias, historial) se guardan en hora de Lima, no en UTC
ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    TZ=America/Lima \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k -XX:+ExitOnOutOfMemoryError -Duser.timezone=America/Lima"

# Capas del jar ordenadas de menos a mas cambiantes para aprovechar la cache
COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./

EXPOSE 8080

# Render inyecta PORT; Spring la lee via server.port=${PORT:8080}
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
