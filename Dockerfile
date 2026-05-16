FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY gradle gradle
COPY gradlew gradlew
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY src src

RUN chmod -R ugo+x gradlew
RUN ./gradlew bootJar --no-daemon

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "build/libs/english-words-app-0.0.1-SNAPSHOT.jar"]
