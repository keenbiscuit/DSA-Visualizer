#Use maven image with java 17, needs AS build to name since this is a multi stage build
FROM maven:3.9-eclipse-temurin-17 AS build

#Starting point for docker, tells it where code will be
WORKDIR /app

#Copy source code
COPY . /app

#Install dependencies
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

#Copy the jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

#Run the jar file
CMD ["java", "-jar", "app.jar"]