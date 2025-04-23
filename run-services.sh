#!/bin/bash

echo "Starting all services..."

docker-compose up -d

echo "All services are running!"
echo "API Gateway is available at http://localhost:8083"
