# Stage 1: Build the application
FROM gradle:jdk21-alpine AS build

WORKDIR /App

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

RUN gradle wrapper

COPY src src

RUN chmod +x gradlew && ./gradlew clean bootJar

# Stage 2: Run the application
FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /App

COPY --from=build /App/build/libs/*.jar app.jar

CMD ["app.jar"]
