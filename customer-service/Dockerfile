FROM eclipse-temurin:21-jdk
WORKDIR /app

# Dùng đúng file jar được tạo bởi bootJar
COPY build/libs/customer-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
