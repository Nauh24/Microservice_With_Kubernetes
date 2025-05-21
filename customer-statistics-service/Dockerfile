FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "-Dspring.devtools.restart.enabled=true", "-Dspring.devtools.add-properties=true", "/app/app.jar"]
