# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .

# 1. Create the Vaadin configuration directory
RUN mkdir -p /root/.vaadin

# 2. Write your license key to the file system (The Bulletproof Way)
# This creates the 'proKey' file exactly where Vaadin looks for it.
RUN echo '{"username":"s.m.ishaq2@gmail.com","proKey":"pro-008d3dce-8a7d-4137-9395-4aff152d31ba"}' > /root/.vaadin/proKey

# 3. Build the app (Now it will find the file and pass validation)
RUN mvn clean package -DskipTests -Pproduction

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
COPY --from=build /target/*.jar app.jar

# Limit RAM for Render Free Tier
ENV JAVA_TOOL_OPTIONS="-Xmx384m"

ENTRYPOINT ["java","-jar","/app.jar"]