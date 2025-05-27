FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/customer-service-0.0.1-SNAPSHOT.jar customer-service.jar
ENTRYPOINT ["java", "-jar", "/customer-service.jar"]
