# Stage 1: Build the application
FROM gradle:jdk21 AS build

WORKDIR /App

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

RUN gradle wrapper

COPY src src

RUN chmod +x gradlew && ./gradlew clean bootJar

# Stage 2: Run the application
FROM amazoncorretto:21

WORKDIR /App

COPY --from=build /App/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]