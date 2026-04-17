FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -Dmaven.test.skip=true
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p /app/logs

EXPOSE 8081 8443

ENTRYPOINT ["java","-Xms256m","-Xmx512m","-jar","app.jar"]