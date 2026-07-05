FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /workspace

COPY pom.xml ./
COPY src src
RUN mvn -DskipTests package

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
