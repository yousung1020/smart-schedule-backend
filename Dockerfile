# 빌드 스테이지
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Gradle 래퍼 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 미리 다운로드
RUN ./gradlew dependencies --no-daemon

# 소스 복사 및 빌드 (테스트는 CI 단계에서 수행하므로 제외하여 속도 향상)
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# 실행 스테이지
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# healthcheck용 curl 설치 후 비루트 사용자 생성
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd -m spring
USER spring

COPY --from=build /app/build/libs/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
