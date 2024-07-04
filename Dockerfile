FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /application
ARG JAR_FILE=build/libs/faf-java-api-*.jar
COPY ${JAR_FILE} application.jar
COPY test-pki-private.key /pki/secret.key
COPY test-pki-public.key /pki/public.key
COPY test-account-activation.html /config/mail/account-activation.html
COPY test-password-reset.html /config/mail/password-reset.html
COPY test-welcome-to-faf.html /config/mail/welcome-to-faf.html
ENV FAF_DOMAIN=faforever.com
RUN java -Djarmode=tools -jar application.jar extract
RUN java -Dspring.context.exit=onRefresh -Dspring.profiles.active=training -XX:ArchiveClassesAtExit=application.jsa -jar application/application.jar

FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
WORKDIR /application
COPY --from=builder /application/application/lib ./
RUN true
COPY --from=builder /application/application/application.jar ./
RUN true
COPY --from=builder /application/application.jsa ./
RUN true
ENTRYPOINT ["java", "-XX:SharedArchiveFile=application.jsa", "-Djava.security.egd=file:/dev/./urandom", "-jar", "application.jar"]
