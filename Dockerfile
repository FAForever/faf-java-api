FROM frolvlad/alpine-java:jdk8-slim

VOLUME /tmp
COPY build/libs/faf-java-api-*.jar app.jar
ENTRYPOINT ["java", "-server", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
