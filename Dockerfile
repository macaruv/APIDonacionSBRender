# Etapa 1: Construcción
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/APIProcesoDonacion-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/secret/apiprocesodonacion-firebase-adminsdk-a9a63-60fca7c380.json /app/secret/apiprocesodonacion-firebase-adminsdk-a9a63-60fca7c380.json
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
