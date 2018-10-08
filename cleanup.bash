#!/usr/bin/env bash

docker-compose down -v --rmi local
docker-compose -f docker-compose-maven.yaml down -v --rmi local

docker rm -f -v $(docker ps -a|grep -v CONTAINER|grep -v kafka|awk '{print $1}') || echo no countainers found.

for container in $(docker ps|grep -v CONTAINER|awk '{print $1}'); do
  docker rm -f -v $container
done

for volume in $(docker volume ls|grep -v DRIVER|awk '{print $2}'); do
  docker volume rm $volume
done

for image in $(docker images -f "dangling=true" -q); do
  docker rmi -f $image
done
