# syntax=docker/dockerfile:1

# ---- build ----------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Resolve dependencies first so that a source-only change does not invalidate
# the dependency layer.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- runtime --------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup --system zerofake && adduser --system --ingroup zerofake zerofake

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

USER zerofake

EXPOSE 8085

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
