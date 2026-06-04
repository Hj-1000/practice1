# 📍 맛집 기록 서비스 (My Gourmet Map)

사용자의 현재 위치를 기반으로 맛집을 지도에 기록하고 관리하는 **위치 기반 웹 서비스**입니다. 단순한 CRUD 구현을 넘어, AWS 인프라 구축부터 HTTPS 보안 적용, CI/CD 자동화 배포까지 **백엔드 개발의 전 과정을 직접 설계하고 운영**한 프로젝트입니다.

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

```mermaid
graph LR
    User[Client] -->|HTTPS/443| Nginx[Nginx Reverse Proxy]
    Nginx -->|Proxy| App[Spring Boot]
    App -->|Data| DB[MariaDB / AWS RDS]

---

💡 Troubleshooting (주요 이슈 해결)
1. HTTPS 적용 및 리다이렉션 이슈

이슈: 도메인 적용 후 외부 접속 시 ERR_CONNECTION_TIMED_OUT 발생 및 OAuth2 리다이렉트 실패

해결:

AWS 보안 그룹(Inbound)에 80/443 포트 규칙을 추가하여 통신 통로 확보

Nginx 서버 블록에 SSL 인증서 경로를 설정하고, 80(HTTP) 요청을 443(HTTPS)으로 자동 리다이렉트 처리

구글/네이버 OAuth2 설정 콘솔에 https://hj1000.duckdns.org 기반의 리다이렉트 URI를 추가하여 redirect_uri_mismatch 문제 해결

2. 자동 배포 파이프라인 구축

이슈: 수동 배포 시 서비스 다운타임 발생 및 환경 설정 파일 관리의 어려움

해결:

GitHub Actions를 사용하여 코드 푸시 시 자동으로 빌드 및 EC2 배포가 이루어지는 CI/CD 파이프라인 구축

-Dspring.config.location 옵션을 활용하여 환경별(DB, OAuth) 설정 파일을 외부 경로에서 주입함으로써 보안성 강화
