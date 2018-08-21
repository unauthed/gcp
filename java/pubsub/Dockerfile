FROM openjdk:8-jdk-alpine

MAINTAINER GDG-Bristol <gdg@unauthed.io>

VOLUME /tmp

WORKDIR /opt

HEALTHCHECK --interval=9s --timeout=5s --retries=3 CMD wget --quiet -O/dev/null http://127.0.0.1:8080/actuator/health || exit 1

ENV JAVA_OPTS=-Xmx512m -XX:MaxMetaspaceSize=128m -Djava.security.egd=file:/dev/./urandom
CMD exec java -XX:+PrintCommandLineFlags $JAVA_OPTS -jar app.jar

ARG ARTIFACT_FILE
COPY ./target/${ARTIFACT_FILE} /opt/app.jar
RUN sh -c 'touch /opt/app.jar'
