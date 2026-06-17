# ─────────────────────────────────────────────────────────────────────────────
# Web server container: JDK 25 + Maven + Git + NGINX + OpenSSH.
# Builds this Spring Boot project, runs it internally on 8081, and fronts it with
# NGINX on 8080. SSH is available on 22.
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk

ENV DEBIAN_FRONTEND=noninteractive

# ---- Install required tooling ----
RUN apt-get update && apt-get install -y --no-install-recommends \
        maven \
        git \
        nginx \
        openssh-server \
        ca-certificates \
        curl \
    && rm -rf /var/lib/apt/lists/*

# ---- OpenSSH: permit root login with a password ----
RUN mkdir -p /var/run/sshd \
    && echo 'root:Hello@123' | chpasswd \
    && sed -i 's/^#\?PermitRootLogin.*/PermitRootLogin yes/' /etc/ssh/sshd_config \
    && sed -i 's/^#\?PasswordAuthentication.*/PasswordAuthentication yes/' /etc/ssh/sshd_config

WORKDIR /app

# ---- Copy the project and build the executable WAR ----
COPY . /app
RUN mvn -B -ntp clean package -DskipTests \
    && cp target/*.war /app/app.war

# ---- NGINX: listen on 8080 and proxy to Spring Boot on 127.0.0.1:8081 ----
RUN rm -f /etc/nginx/sites-enabled/default \
    && cp /app/docker/nginx.conf /etc/nginx/conf.d/default.conf \
    && chmod +x /app/docker/start.sh

# 8080 = NGINX (website), 22 = SSH
EXPOSE 8080 22

CMD ["/app/docker/start.sh"]
