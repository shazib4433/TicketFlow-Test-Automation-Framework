FROM maven:3.9.16-eclipse-temurin-26 AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY application/pom.xml application/pom.xml
COPY application/src application/src
RUN ./mvnw -B -ntp -pl application package -DskipTests

FROM eclipse-temurin:26-jre
WORKDIR /app
COPY --from=build /workspace/application/target/ticketflow-application-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
