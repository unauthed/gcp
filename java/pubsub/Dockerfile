FROM openjdk:8-jdk-alpine

LABEL Description="This image is used to build Spring Boot artifacts with the Google Maven App Engine plugin."
LABEL Maintainer="GDG Bristol <gdg@unauthed.io>"
LABEL GDG="Bristol"

VOLUME /tmp

WORKDIR /opt

HEALTHCHECK --interval=9s --timeout=5s --retries=3 CMD wget --quiet -O/dev/null http://127.0.0.1:8080/actuator/health || exit 1

ARG JAVA_OPTS="-Xmx256m -XX:MaxMetaspaceSize=128m -Djava.security.egd=file:/dev/./urandom"
CMD exec java -XX:+PrintCommandLineFlags ${JAVA_OPTS} -jar app.jar

ARG ARTIFACT_FILE="*.jar"
COPY ./target/${ARTIFACT_FILE} /opt/app.jar
RUN sh -c 'touch /opt/app.jar'
