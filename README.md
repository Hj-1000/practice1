# 💡 Service Availability
본 서비스는 안정적인 운영 환경 효율화를 위해 **평일 10:00 ~ 18:00 (KST)**에 운영됩니다. 운영 시간 외 접속 시 서버가 일시 중지될 수 있는 점 양해 부탁드립니다.
---

# 📍 맛집 기록 서비스 (My Gourmet Map)

## 사용자의 현재 위치를 기반으로 내가 등록한 식당을 지도에 기록하고 관리하는 **위치 기반 웹 서비스**입니다. 단순한 CRUD 구현을 넘어, AWS 인프라 구축부터 HTTPS 보안 적용, CI/CD 자동화 배포까지 **백엔드 개발의 전 과정을 직접 설계하고 운영**한 프로젝트입니다.

---

## 🔗 Live Demo

**Service URL**

https://hj1000.duckdns.org

**GitHub Repository**

https://github.com/Hj-1000/practice1

**호텔예약프로그램(팀프로젝트)**  https://github.com/Hj-1000/HotelProj

---

## 🛠 Tech Stack

### Backend
* **Language:** Java 21
* **Framework:** Spring Boot 3.5.0
* **Persistence:** Spring Data JPA, MariaDB
* **Security:** Spring Security, Google/Naver OAuth2

### Frontend
* **UI:** Mustache, Bootstrap 5, JavaScript

### DevOps & Infra
* **CI/CD:** GitHub Actions
* **Infra:** AWS EC2, AWS RDS, Nginx
* **Security:** Let's Encrypt (SSL/TLS), HTTPS
* **Domain:** DuckDNS

---

## 🚀 Key Features

* **사용자 인증:** Google/Naver OAuth2를 활용한 간편 로그인 및 Spring Security 기반 인가 처리
* **위치 기반 필터링:** 브라우저 Geolocation API와 연동하여 현재 위치에서 **1km 이내 맛집 조회** (좌표 기반 거리 계산 알고리즘 구현)
* **지도 통합:** Kakao Maps API를 활용한 맛집 위치 시각화 및 마커 표시
* **맛집 기록:** 게시글 CRUD 및 사용자별 개인화된 맛집 관리

---

## 🏗 System Architecture

<img width="1152" height="648" alt="Image" src="https://github.com/user-attachments/assets/f4b93172-109e-4b0a-a20e-b4b86fe8ca7a" />

---

## 💡 Troubleshooting (주요 이슈 해결)

### 1. HTTPS 적용 및 리다이렉션 이슈

#### 문제

도메인 적용 후 외부 접속 시 `ERR_CONNECTION_TIMED_OUT` 발생 및 OAuth2 로그인 실패

#### 원인

- AWS 보안 그룹에서 80, 443 포트가 열려 있지 않음
- HTTPS 환경에 맞는 OAuth2 Redirect URI 미설정

#### 해결

- AWS Security Group Inbound Rule에 80, 443 포트 추가
- Nginx SSL 인증서 적용
- HTTP → HTTPS 자동 리다이렉트 설정
- Google OAuth2, Naver OAuth2 콘솔에 HTTPS Redirect URI 등록

#### 결과

- HTTPS 환경 정상 구축
- OAuth2 로그인 정상 동작
- 외부 접속 가능

---

### 2. GitHub Actions 기반 자동 배포 구축

#### 문제

수동 배포 시 다음과 같은 문제가 존재

- 배포 과정이 번거로움
- 환경설정 파일 관리 어려움
- 배포 실수 가능성 존재

#### 해결

- GitHub Actions 기반 CI/CD 파이프라인 구축
- Git Push 시 자동 빌드 및 EC2 배포
- `-Dspring.config.location` 옵션을 사용하여 외부 설정 파일 주입
- DB 계정 및 OAuth 설정을 애플리케이션 외부에서 관리

#### 결과

- 배포 자동화
- 환경 설정 분리
- 운영 편의성 향상

---

### 3. 위치 기반 맛집 조회 기능 구현

#### 문제

사용자의 현재 위치를 기반으로 일정 거리 내 맛집을 조회해야 함

#### 해결

- 브라우저 Geolocation API 활용
- 위도·경도 좌표 획득
- 좌표 간 거리 계산 로직 구현
- 반경 1km 이내 맛집만 필터링

#### 결과

- 사용자 주변 맛집 탐색 기능 제공
- 실제 서비스 환경에서 동작 확인

---

### 4. HTTPS 미적용으로 인한 GPS 기능 동작 실패

#### 문제

로컬 환경에서는 GPS 기능이 정상 동작했으나 EC2 배포 환경에서는 위치 조회 실패

#### 원인

Geolocation API는 HTTPS 환경에서만 동작

#### 해결

- DuckDNS 도메인 연결
- Let's Encrypt SSL 인증서 발급
- Nginx HTTPS 적용

#### 결과

- 배포 환경에서도 GPS 기반 위치 조회 가능

---

## 📚 What I Learned

- OAuth2 인증/인가 구조 이해
- Spring Security 실무 적용 경험
- AWS EC2, RDS 운영 경험
- Nginx Reverse Proxy 및 SSL 구축 경험
- GitHub Actions 기반 CI/CD 구축 경험
- 위치 기반 서비스(GPS) 개발 경험
- HTTPS와 브라우저 보안 정책 이해
- 실제 서비스 배포 및 운영 경험

---

## 🚀 Future Improvements

- Redis 캐시 적용
- Docker 기반 컨테이너 환경 구성
- QueryDSL 고도화
- Prometheus + Grafana 모니터링 구축
- 무중단 배포(Blue-Green) 적용
- 인기 맛집 랭킹 기능 추가


