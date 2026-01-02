# Stage 1: Build the application (Using Java 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests -Pproduction

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
COPY --from=build /target/*.jar app.jar

# CRITICAL: Limit RAM so Render Free Tier doesn't crash
ENV JAVA_TOOL_OPTIONS="-Xmx384m"

ENTRYPOINT ["java","-jar","/app.jar"]