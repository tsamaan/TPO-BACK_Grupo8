## Multi-stage Dockerfile for building and running the Spring Boot app
FROM eclipse-temurin:17-jdk-jammy as build
WORKDIR /workspace

# Copy maven wrapper and pom first to leverage Docker cache
COPY mvnw mvnw.cmd pom.xml ./
COPY src src

RUN chmod +x ./mvnw && ./mvnw -DskipTests package -P !native

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
