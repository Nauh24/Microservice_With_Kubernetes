FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/customer-payment-service-0.0.1-SNAPSHOT.jar customer-payment-service.jar
ENTRYPOINT ["java", "-jar", "/customer-payment-service.jar"]
