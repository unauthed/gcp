#!/usr/bin/env bash

docker-compose up -d

browse http://localhost:8080

mvn spring-boot:run

