FROM eclipse-temurin:17-jdk 
WORKDIR /app 
COPY target/*.jar app.jar 
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "/app/app.jar"]
