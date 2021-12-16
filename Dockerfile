FROM eclipse-temurin:17-jdk-alpine as builder
WORKDIR /application
ARG JAR_FILE=build/libs/faf-java-api-*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
WORKDIR /application
COPY --from=builder /application/dependencies/ ./
RUN true
COPY --from=builder /application/spring-boot-loader/ ./
RUN true
COPY --from=builder /application/snapshot-dependencies/ ./
RUN true
COPY --from=builder /application/application/ ./
RUN true
ENTRYPOINT ["java", "-server", "-Djava.security.egd=file:/dev/./urandom", "org.springframework.boot.loader.JarLauncher"]
