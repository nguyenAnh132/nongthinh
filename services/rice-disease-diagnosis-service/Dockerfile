
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -Dmaven.test.skip=true package && \
    cp target/*.jar /build/app.jar

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN groupadd --gid 10001 app && \
    useradd --uid 10001 --gid app --no-create-home --shell /usr/sbin/nologin app

ENV SPRING_PROFILES_ACTIVE=prod

COPY --from=build --chown=10001:10001 /build/app.jar /app/app.jar
USER 10001:10001

EXPOSE 9099
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
