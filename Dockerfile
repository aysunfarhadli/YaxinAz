FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew --version
COPY src src
RUN ./gradlew clean bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN useradd --uid 1000 --create-home yaxinaz
COPY --from=build /workspace/build/libs/*.jar app.jar
RUN mkdir -p /app/uploads && chown -R yaxinaz:yaxinaz /app
USER yaxinaz
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
