FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/job-service-0.0.1-SNAPSHOT.jar job-service.jar
ENTRYPOINT ["java", "-jar", "/job-service.jar"]
