# Customer Contract Service Docker Image
FROM openjdk:17-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /app

# Copy the JAR file
COPY target/customer-contract-service-0.0.1-SNAPSHOT.jar customer-contract-service.jar

# Add health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

# Expose port
EXPOSE 8083

# Run the application
ENTRYPOINT ["java", "-jar", "customer-contract-service.jar"]
