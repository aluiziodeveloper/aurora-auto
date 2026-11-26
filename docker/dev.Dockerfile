# Development container: JDK 25 and Maven 3.9.16 at fixed versions, running as your user.
FROM maven:3.9.16-eclipse-temurin-25

ARG UID=1000
ARG GID=1000

# A user with the same UID/GID as the host, so files created in /src belong to you.
# The base image already has a user with UID 1000 (ubuntu); it gives way to ours.
# The Maven cache directory is created here so the named volume inherits its owner.
RUN if id -u ubuntu >/dev/null 2>&1; then userdel --remove ubuntu; fi \
 && groupadd --gid "${GID}" dev \
 && useradd --uid "${UID}" --gid "${GID}" --create-home dev \
 && mkdir -p /home/dev/.m2 \
 && chown -R dev:dev /home/dev/.m2

# The base image points MAVEN_CONFIG at /root/.m2; our user keeps its cache in its own home.
ENV MAVEN_CONFIG=""

USER dev
WORKDIR /src
