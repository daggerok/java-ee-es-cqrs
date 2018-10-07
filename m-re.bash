#!/usr/bin/env bash
bash ./cleanup.bash
./mvnw clean:clean war:war
#docker-compose -f docker-compose-maven.yaml build --pull
docker-compose -f docker-compose-maven.yaml up -V --always-recreate-deps &
docker-compose -f docker-compose-maven.yaml logs -f -t &
