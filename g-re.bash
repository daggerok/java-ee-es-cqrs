#!/usr/bin/env bash
bash ./cleanup.bash
./gradlew clean war
#docker-compose build --pull
docker-compose up -V --always-recreate-deps &
docker-compose logs -f -t &
