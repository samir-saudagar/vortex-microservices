#!/bin/bash
echo "Starting VORTEX Infrastructure..."
podman-compose up -d
echo "Infrastructure is running (Postgres: 5432, Kafka: 9092)."

