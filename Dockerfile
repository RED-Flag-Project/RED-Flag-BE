# 1. 빌드 스테이지
FROM amazoncorretto:17-alpine-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# 2. 실행 스테이지
FROM amazoncorretto:17-alpine-jdk
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# JVM 메모리 제한 설정
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
