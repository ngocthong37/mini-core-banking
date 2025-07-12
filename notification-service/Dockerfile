FROM openjdk:17-jdk-alpine
WORKDIR /app

# Dùng đúng file jar được tạo bởi bootJar
COPY build/libs/notification-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
