FROM openjdk:8-jdk-alpine

LABEL Description="Spring Boot application image."
LABEL Maintainer="GDG Bristol <gdg@unauthed.io>"
LABEL GDG="Bristol"

VOLUME /tmp
WORKDIR /opt

HEALTHCHECK --interval=9s --timeout=5s --retries=3 CMD wget --quiet -O/dev/null http://127.0.0.1:8080/actuator/health || exit 1

ARG JAVA_OPTIONS="-Xmx512m -XX:MaxMetaspaceSize=128m -Djava.security.egd=file:/dev/./urandom"
CMD exec java -XX:+PrintCommandLineFlags ${JAVA_OPTIONS} -jar app.jar

ARG ARTIFACT_FILE="target/*.jar"
COPY ${ARTIFACT_FILE} /opt/app.jar
RUN sh -c 'touch /opt/app.jar'
