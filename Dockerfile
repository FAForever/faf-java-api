FROM eclipse-temurin:25-jdk-alpine as builder
WORKDIR /application
ARG JAR_FILE=build/libs/faf-java-api-*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM eclipse-temurin:25-jdk-alpine
VOLUME /tmp
WORKDIR /application
COPY --from=builder /application/extracted/dependencies/ ./
RUN true
COPY --from=builder /application/extracted/spring-boot-loader/ ./
RUN true
COPY --from=builder /application/extracted/snapshot-dependencies/ ./
RUN true
COPY --from=builder /application/extracted/application/ ./
RUN true
ENTRYPOINT ["java", "-server", "-Djava.security.egd=file:/dev/./urandom", "-jar", "application.jar"]
