# Stage 1: build the jar with Maven. Tests run in CI, not in the image build.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY service/pom.xml ./pom.xml
COPY service/src ./src
RUN mvn -B -DskipTests package

# Stage 2: run the jar on a JRE-only base image.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/lab04-service.jar ./lab04-service.jar
COPY run.sh ./run.sh
RUN chmod +x ./run.sh && useradd --system --no-create-home --shell /usr/sbin/nologin service
USER service

# PORT is what the service binds. EXPOSE documents the default for tooling.
ENV PORT=8080
EXPOSE 8080
CMD ["sh", "run.sh"]
