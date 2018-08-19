#!/usr/bin/env bash

echo remove containers
docker rm $(docker ps -q -a)

echo remove images
docker rmi $(docker images -q -a)

echo remove volumes
docker volume rm $(docker volume ls -q -f dangling=true)

echo remove networks
docker network rm $(docker network ls -q)


