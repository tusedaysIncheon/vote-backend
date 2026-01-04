# Vote Backend

이 프로젝트는 투표 애플리케이션의 백엔드 서버로, Spring Boot와 Java 17을 기반으로 구축되었습니다. 사용자 인증, 소셜 로그인, JWT 기반의 API 보안 등 다양한 기능을 제공합니다.

## 🚀 주요 기술 스택

- **Framework**: Spring Boot 3.5.6, Spring Security
- **Language**: Java 17
- **Database**: PostgreSQL, Redis
- **Authentication**: JWT (JSON Web Tokens)
- **API Documentation**: SpringDoc OpenAPI

## ✨ 시작하기

### 사전 요구사항

- Java 17
- Gradle
- PostgreSQL 데이터베이스
- Redis 서버

### 빌드 및 실행

1.  **애플리케이션 클론:**

    ```bash
    git clone https://github.com/your-username/vote-backend.git
    cd vote-backend
    ```

2.  **`application.yml` 설정:**

    `src/main/resources/application.yml` 파일을 열어 데이터베이스, Redis, JWT 및 OAuth2 클라이언트 설정을 환경에 맞게 수정합니다.

    ```yaml
    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/your_db
        username: your_username
        password: your_password
      redis:
        host: localhost
        port: 6379
      security:
        oauth2:
          client:
            registration:
              google:
                client-id: "your-google-client-id"
                client-secret: "your-google-client-secret"
              # 다른 소셜 로그인 설정 추가
    jwt:
      secret: "your-jwt-secret-key"
    ```

3.  **애플리케이션 빌드 및 실행:**

    ```bash
    ./gradlew bootRun
    ```

    이제 `http://localhost:8080`에서 애플리케이션이 실행됩니다.

## 📁 프로젝트 구조

```
.
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com/vote/votebackend
│   │   │       ├── api         # API 컨트롤러
│   │   │       ├── config      # 보안, MVC, JWT 등 설정
│   │   │       ├── domain      # 도메인 엔티티, DTO, 리포지토리, 서비스
│   │   │       ├── filter      # JWT 및 로그인 필터
│   │   │       ├── handler     # 로그인 성공/실패 핸들러
│   │   │       └── util        # JWT 유틸리티
│   │   └── resources
│   │       └── application.yml # 애플리케이션 설정
│   └── test                    # 테스트 코드
└── docs
    └── api-spec.md             # API 명세서
```

## 🔐 주요 기능

### 1. 사용자 인증 및 인가

- **자체 로그인**: 이메일/비밀번호 기반의 로그인을 지원하며, 성공 시 JWT를 발급합니다.
- **소셜 로그인**: Google, Naver, Kakao 등 OAuth2를 통한 소셜 로그인을 지원합니다. 최초 로그인 시 사용자 정보를 저장하고, 이후 JWT를 통해 인증합니다.
- **JWT 기반 인증**:
  - **Access Token**: API 요청 시 `Authorization: Bearer <token>` 헤더에 담아 전송되며, 서버에서는 `JWTFilter`를 통해 유효성을 검증합니다.
  - **Refresh Token**: Access Token이 만료되었을 때 재발급을 위해 사용됩니다. Redis에 저장되어 보안을 강화하고, 로그아웃 시 서버에서 삭제됩니다.

### 2. API 엔드포인트

자세한 API 명세는 [docs/api-spec.md](docs/api-spec.md) 파일에서 확인할 수 있습니다.

#### JWT API (`/jwt`)

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| POST | `/jwt/exchange` | 소셜 로그인 후 Refresh 토큰을 Access 토큰으로 교환합니다. |
| POST | `/jwt/refresh` | Refresh 토큰을 사용하여 새로운 Access 토큰을 재발급합니다. |

#### 사용자 API (`/v1/user`)

| 메서드 | 경로 | 설명 |
| --- | --- | --- |
| POST | `/exist` | 사용자 아이디 중복 여부를 확인합니다. |
| POST | `/` | 신규 사용자를 등록합니다. |
| GET | `/` | 현재 로그인된 사용자의 정보를 조회합니다. |
| PUT | `/` | 사용자 정보를 수정합니다. |
| DELETE | `/` | 회원에서 탈퇴합니다. |
| PATCH | `/nickname` | 소셜 로그인 사용자의 닉네임을 설정/변경합니다. |
| POST | `/login` | 자체 로그인을 수행합니다. |

### 3. 예외 처리

`@RestControllerAdvice`를 사용하여 전역적으로 예외를 처리하고, 일관된 형식의 에러 메시지를 반환합니다.

## 📦 주요 의존성

- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-data-redis`
- `spring-boot-starter-oauth2-client`
- `spring-boot-starter-validation`
- `jjwt` (Java JWT)
- `postgresql` (PostgreSQL JDBC 드라이버)
- `springdoc-openapi-starter-webmvc-ui` (API 문서 자동화)
- `lombok`