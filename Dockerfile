FROM adoptopenjdk/openjdk11:alpine-jre

VOLUME /tmp
COPY build/libs/faf-java-api-*.jar app.jar
ENTRYPOINT ["java", "-server", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
