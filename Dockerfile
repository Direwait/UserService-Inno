FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY target/internship-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${SPRING_PROFILE:docker}"]