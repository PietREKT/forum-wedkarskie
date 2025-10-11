FROM maven:3.9-eclipse-temurin-21 AS deps

WORKDIR /app
COPY pom.xml /app

RUN mvn -B dependency:go-offline dependency:resolve-plugins
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY --from=deps /root/.m2/repository /root/.m2/repository
COPY ./entrypoint.sh /entrypoint.sh
COPY . .

RUN apt-get update && apt-get install -y inotify-tools
RUN chmod +x /entrypoint.sh


ENTRYPOINT [ "/entrypoint.sh"]
