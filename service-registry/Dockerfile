FROM eclipse-temurin:21-jdk
WORKDIR /app

# Dùng đúng file jar được tạo bởi bootJar
COPY build/libs/service-registry-1.0.0.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
