FROM gradle:8.12.0-jdk AS builder
WORKDIR /usr/app
COPY . .

RUN ./gradlew build

FROM adoptopenjdk/openjdk11:alpine-jre

COPY --from=builder /usr/app/app/build/libs/app.jar /usr/bin/app

ENTRYPOINT ["java", "-jar", "/usr/bin/app"]

LABEL org.opencontainers.image.source=https://github.com/ufsay/mc_missile_guidance
