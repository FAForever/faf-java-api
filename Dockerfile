FROM frolvlad/alpine-oraclejdk8:slim

MAINTAINER Michel Jung <michel.jung89@gmail.com>

VOLUME /tmp
COPY build/libs/faf-java-api-*.jar app.jar
ENTRYPOINT ["java", "-server", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
