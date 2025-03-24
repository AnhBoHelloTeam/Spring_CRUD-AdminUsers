# Sử dụng image OpenJDK chính thức làm base image
FROM openjdk:17-jdk-slim

# Thêm metadata
LABEL maintainer="nhanhateku1@gmail.com"

# Đảm bảo file .jar đã được build từ Maven
ARG JAR_FILE=target/demo_spring-0.0.1-SNAPSHOT.jar

# Sao chép file .jar vào Docker image
COPY ${JAR_FILE} /app.jar

# Chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "/app.jar"]

# Mở port 8080 cho ứng dụng Spring Boot
EXPOSE 8080
