FROM openjdk:17-jdk-alpine
WORKDIR /app

# Dùng đúng file jar được tạo bởi bootJar
COPY build/libs/auth-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
