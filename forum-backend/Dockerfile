FROM maven:3.9-eclipse-temurin-21 AS dev
WORKDIR /app
COPY pom.xml .

# RUN mvn -B dependency:go-offline

COPY src ./src

EXPOSE 8080
ENTRYPOINT ["mvn", "spring-boot:run"]
