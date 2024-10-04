FROM openjdk:17-jdk-slim
ARG JAR_FILE=build/libs/*.jar
# 작업 디렉토리 설정 (필요한 경우)
WORKDIR /app
COPY ${JAR_FILE} app.jar
COPY src/main/resources/application-prod.yml /app/src/main/resources/application-prod.yml
# Firebase 인증키 파일을 지정된 경로로 복사
COPY src/main/resources/firebase/gnu-gongji-firebase-adminsdk-p9h0v-4fed761b99.json /app/src/main/resources/firebase/gnu-gongji-firebase-adminsdk-p9h0v-4fed761b99.json
ENV TZ=Asia/Seoul
RUN apt-get update && apt-get install -y tzdata && rm -rf /var/lib/apt/lists/*
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]