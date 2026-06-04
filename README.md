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
