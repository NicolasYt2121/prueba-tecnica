FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY backend/pom.xml .
RUN mvn -B dependency:go-offline
COPY backend/src ./src
RUN mvn -B package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S nexo && adduser -S nexo -G nexo
WORKDIR /app
COPY --from=build /build/target/banking-1.0.0.jar app.jar
USER nexo
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
