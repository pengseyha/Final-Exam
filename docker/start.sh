#!/usr/bin/env bash
# Entry point for the web container: start SSH + NGINX in the background, then
# run the Spring Boot app in the foreground (PID 1) so the container stays alive.
set -euo pipefail

echo "[start] Launching OpenSSH server on port 22 ..."
mkdir -p /var/run/sshd
/usr/sbin/sshd

echo "[start] Launching NGINX on port 8080 (proxy -> 127.0.0.1:8081) ..."
nginx

echo "[start] Launching Spring Boot on port 8081 ..."
exec java -jar /app/app.war --server.port=8081
