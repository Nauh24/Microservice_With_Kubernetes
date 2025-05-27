FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/customer-contract-service-0.0.1-SNAPSHOT.jar customer-contract-service.jar
ENTRYPOINT ["java", "-jar", "/customer-contract-service.jar"]
