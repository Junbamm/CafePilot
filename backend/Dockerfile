# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 의존성 레이어 캐시 최적화: gradle 파일만 먼저 복사
COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle ./gradle

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet

# 소스 코드 복사 및 빌드
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# Layered JAR 추출
RUN java -Djarmode=layertools -jar build/libs/*.jar extract --destination build/extracted

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# 보안: 비root 유저 생성
RUN addgroup -S cafepilot && adduser -S cafepilot -G cafepilot
USER cafepilot

# Layered JAR 레이어 순서대로 복사 (캐시 효율 최대화)
COPY --from=builder /app/build/extracted/dependencies ./
COPY --from=builder /app/build/extracted/spring-boot-loader ./
COPY --from=builder /app/build/extracted/snapshot-dependencies ./
COPY --from=builder /app/build/extracted/application ./

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "org.springframework.boot.loader.launch.JarLauncher"]
