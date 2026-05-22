# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
# Remove Windows line endings if present 
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copy the built jar from the build stage
COPY --from=build /app/build/libs/Yomu-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
