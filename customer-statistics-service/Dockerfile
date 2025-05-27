FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/customer-statistics-service-0.0.1-SNAPSHOT.jar customer-statistics-service.jar
ENTRYPOINT ["java", "-jar", "/customer-statistics-service.jar"]
