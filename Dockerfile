# Stage 1: Build the application (Using Java 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests -Pproduction -Dvaadin.proKey="s.m.ishaq2@gmail.com:pro-008d3dce-8a7d-4137-9395-4aff152d31ba"
#{"username":"s.m.ishaq2@gmail.com","proKey":"pro-008d3dce-8a7d-4137-9395-4aff152d31ba"}

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
COPY --from=build /target/*.jar app.jar

# CRITICAL: Limit RAM so Render Free Tier doesn't crash
ENV JAVA_TOOL_OPTIONS="-Xmx384m"

ENTRYPOINT ["java","-jar","/app.jar"]