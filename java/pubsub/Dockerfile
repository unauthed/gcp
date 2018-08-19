FROM openjdk:8-jdk-alpine

MAINTAINER GDG-Bristol <gdg@unauthed.io>

VOLUME /tmp

WORKDIR /opt

HEALTHCHECK --interval=9s --timeout=5s --retries=3 CMD wget --quiet -O/dev/null http://127.0.0.1:9001/actuator/health || exit 1

ENV JAVA_OPTS=-Xmx256m -XX:MaxMetaspaceSize=128m -Djava.security.egd=file:/dev/./urandom
CMD exec java -XX:+PrintCommandLineFlags $JAVA_OPTS -jar app.jar

ARG JAR_FILE
COPY ./target/${JAR_FILE} /opt/app.jar
RUN sh -c 'touch /opt/app.jar'
