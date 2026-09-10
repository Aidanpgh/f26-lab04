#!/bin/sh
# Boots the built service jar on $PORT (default 8080), in the container or bare.
set -eu

PORT="${PORT:-8080}"
export PORT

HERE="$(dirname "$0")"
if [ -f "$HERE/lab04-service.jar" ]; then
  JAR="$HERE/lab04-service.jar"
else
  JAR="$HERE/service/target/lab04-service.jar"
fi

exec java -jar "$JAR"
