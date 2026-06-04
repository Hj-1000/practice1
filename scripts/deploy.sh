#!/bin/bash

REPOSITORY=/home/ec2-user/app/step3
PROJECT_NAME=practice1

echo "> Build 파일 복사"
cp $REPOSITORY/zip/*.jar $REPOSITORY/

echo "> 현재 구동 중인 애플리케이션 pid 확인"
# jar를 실행한 프로세스 중 practice1이 포함된 PID를 정확히 찾아냅니다.
CURRENT_PID=$(pgrep -f "java -jar.*$PROJECT_NAME" | awk '{print $1}')

echo "> 현재 구동 중인 애플리케이션 pid: $CURRENT_PID"

if [ -z "$CURRENT_PID" ];

then
    echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

echo "> 새 애플리케이션 배포"
# 평문 jar를 제외하고 가장 최신에 들어온 jar 파일을 선택합니다.
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | grep -v 'plain' | tail -n 1)

echo "> JAR Name: $JAR_NAME"

echo "> JAR Name 에 실행권한 추가"
chmod +x $JAR_NAME

if [ 'real1' = $(curl -s http://localhost/profile) ];

then
    IDLE_PROFILE=real2
else
    IDLE_PROFILE=real1
fi


echo "> $JAR_NAME 실행 (Port: $IDLE_PORT 로 실행 시도)"
nohup java -jar \
    -Dspring.config.location=classpath:/application.properties,classpath:/application-real.properties,/home/ec2-user/app/application-oauth.properties,/home/ec2-user/app/application-real-db.properties,/home/ec2-user/app/application-$IDLE_PROFILE.properties \
    -Dspring.profiles.active=$IDLE_PROFILE \
    $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &