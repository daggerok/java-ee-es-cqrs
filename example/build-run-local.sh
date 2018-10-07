#!/bin/bash

docker rm -f -v kafka || echo .
docker pull daggerok/kafka:v10
docker run -d --rm --name kafka \
  -e KAFKA_TPOSICS='order,beans,barista' \
  daggerok/kafka:v10

sleep 5

for i in barista beans orders ; do
  docker build -t daggerok/${i}:1 ${i}/
  docker run -d --rm --name ${i} -p 8080 daggerok/${i}:1
  (sleep 1 && docker logs -f ${i}) &
done
