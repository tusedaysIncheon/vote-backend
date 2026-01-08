# Vote Backend

이 프로젝트는 투표 애플리케이션의 백엔드 서버로, Spring Boot와 Java 17을 기반으로 구축되었습니다. 사용자 인증, 소셜 로그인, JWT 기반의 API 보안 등 다양한 기능을 제공합니다.

## 🚀 주요 기술 스택

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 17
- **Security**: Spring Security, OAuth2 Client, JWT (jjwt)
- **Database**: PostgreSQL (for primary data), Redis (for refresh tokens and rate limiting)
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Build Tool**: Gradle
- **Other**:
    - Lombok: Boilerplate code reduction
    - Bucket4j: Rate limiting
    - Log4j2: Logging
    - Spring Boot Validation: for request data validation

> **⚠️ 주의:** `build.gradle`에 명시된 Spring Boot `3.5.6` 버전은 공식적으로 존재하지 않는 버전입니다. 이로 인해 의존성 관리(dependency resolution)에 문제가 발생할 수 있으며, 프로젝트의 안정성을 위해 공식적으로 릴리스된 버전(예: `3.3.0`)으로 수정하는 것을 권장합니다.

## ✨ 시작하기

### 사전 요구사항

- Java 17
- Gradle
- Docker (for PostgreSQL and Redis) or local installations

### 빌드 및 실행

1.  **애플리케이션 클론:**

    ```bash
    git clone https://github.com/your-username/vote-backend.git
    cd vote-backend
    ```

2.  **데이터베이스 및 Redis 실행 (Docker 사용 시):**

    ```bash
    docker-compose up -d
    ```
    *(Note: A `docker-compose.yml` file is not included in the project, but this is the recommended way to run dependencies.)*

3.  **`application.yml` 설정:**

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
              # Other social providers
    jwt:
      secret: "your-jwt-secret-key-with-at-least-256-bits"
    ```

4.  **애플리케이션 빌드 및 실행:**

    ```bash
    ./gradlew bootRun
    ```

    이제 `http://localhost:8080`에서 애플리케이션이 실행됩니다. API 문서는 `http://localhost:8080/swagger-ui.html`에서 확인할 수 있습니다.


## 📁 프로젝트 구조

```
.
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com/vote/votebackend
│   │   │       ├── api         # API controllers
│   │   │       ├── config      # Security, MVC, JWT configurations
│   │   │       ├── domain      # Domain entities, DTOs, repositories, services
│   │   │       ├── filter      # JWT, RateLimit, and Login filters
│   │   │       ├── handler     # Login success/failure handlers
│   │   │       └── util        # JWT utility
│   │   └── resources
│   │       └── application.yml # Application configuration
│   └── test                    # Test code
└── docs
    └── api-spec.md             # API specification
```

## 🔐 주요 기능

### 1. 사용자 인증 및 인가

- **자체 로그인**: 이메일/비밀번호 기반의 로그인을 지원하며, 성공 시 JWT를 발급합니다.
- **소셜 로그인**: Google, Naver, Kakao 등 OAuth2를 통한 소셜 로그인을 지원합니다. 최초 로그인 시 사용자 정보를 저장하고, 이후 JWT를 통해 인증합니다.
- **JWT 기반 인증**:
  - **Access Token**: API 요청 시 `Authorization: Bearer <token>` 헤더에 담아 전송되며, 서버에서는 `JWTFilter`를 통해 유효성을 검증합니다.
  - **Refresh Token**: Access Token이 만료되었을 때 재발급을 위해 사용됩니다. Redis에 저장되어 보안을 강화하고, 로그아웃 시 서버에서 삭제됩니다.

## 📖 API 엔드포인트

### User API (`/v1/user`)

| Method | Path                | Description                                        | Auth Required | Roles  |
|--------|---------------------|----------------------------------------------------|---------------|--------|
| POST   | `/exist`            | 사용자 아이디 중복 여부를 확인합니다.               | No            | -      |
| POST   | `/`                 | 신규 사용자를 등록합니다.                             | No            | -      |
| GET    | `/`                 | 현재 로그인된 사용자의 정보를 조회합니다.           | Yes           | `USER` |
| PUT    | `/`                 | 사용자 정보를 수정합니다.                             | Yes           | `USER` |
| DELETE | `/`                 | 회원에서 탈퇴합니다.                                | Yes           | `USER` |
| PATCH  | `/nickname`         | 소셜 로그인 사용자의 닉네임을 설정/변경합니다.      | Yes           | `USER` |
| POST   | `/login`            | 자체 로그인을 수행합니다.                             | No            | -      |
| POST   | `/logout`           | 로그아웃을 수행합니다.                               | Yes           | -      |

### JWT API (`/jwt`)

| Method | Path          | Description                                                    | Auth Required |
|--------|---------------|----------------------------------------------------------------|---------------|
| POST   | `/exchange`   | 소셜 로그인 직후 세션 쿠키를 서비스 자체 JWT로 교환합니다.     | No            |
| POST   | `/refresh`    | Refresh 토큰을 사용하여 새로운 Access 토큰을 재발급합니다.     | No            |

## 🛡️ 보안 평가

이 섹션은 프로젝트의 현재 보안 상태에 대한 평가를 제공합니다.

### 잘 구현된 점

- **JWT 기반 인증**: Stateless한 인증 방식을 사용하여 확장성이 좋습니다. Access/Refresh 토큰 분리 전략은 보안을 강화합니다.
- **비밀번호 암호화**: `BCryptPasswordEncoder`를 사용하여 비밀번호를 안전하게 해싱합니다.
- **역할 기반 접근 제어 (RBAC)**: `Spring Security`를 사용하여 엔드포인트별로 세분화된 권한 관리를 하고 있습니다.
- **입력 유효성 검사**: `@Validated`와 `@Valid` 어노테이션을 사용하여 DTO(Data Transfer Object)에 대한 서버사이드 유효성 검사를 적용하여 안정성을 높였습니다.
- **Rate Limiting**: `Bucket4j`를 사용하여 API 요청 제한을 구현하여 DoS 공격 및 Brute-force 공격에 대한 방어책을 마련했습니다.
- **소셜 로그인**: OAuth2를 사용하여 안전하게 소셜 로그인을 구현했습니다.
- **CORS 설정**: 특정 오리진(`http://localhost:5173`)에 대해서만 요청을 허용하도록 설정되어 있어 기본적인 웹 보안을 준수합니다.

### 개선 제안

- **CSRF 보호**: 현재 `CSRF` 보호가 비활성화되어 있습니다. Stateless API에서는 일반적으로 문제가 되지 않지만, Refresh Token 요청과 같이 쿠키를 사용하는 엔드포인트(`/jwt/refresh`)는 CSRF 공격에 취약할 수 있습니다. 쿠키의 `SameSite` 속성을 `Strict`로 설정하여 방어 수준을 높이는 것을 권장합니다.
- **입력 유효성 검사 강화**: 기본적인 유효성 검사는 적용되어 있으나, 모든 API 엔드포인트에서 일관되게 적용되고 있는지, 더 복잡한 비즈니스 규칙(예: 비밀번호 복잡도)에 대한 검증이 충분한지 검토가 필요합니다.
- **민감한 정보 로깅**: 운영 환경에서는 민감한 사용자 정보(예: username)가 로그에 남지 않도록 로그 레벨과 내용을 신중하게 관리해야 합니다.
- **보안 헤더**: `Content-Security-Policy`, `X-Content-Type-Options`, `X-Frame-Options` 등의 보안 관련 HTTP 헤더를 응답에 추가하여 XSS와 같은 웹 취약점을 추가적으로 방어할 수 있습니다.
- **Secrets 관리**: `application.yml`에 포함된 JWT 시크릿 키, 데이터베이스 비밀번호 등은 개발용 예시입니다. 운영 환경에서는 환경 변수나 Vault와 같은 외부 secret 관리 도구를 통해 안전하게 주입해야 합니다.
- **의존성 스캔**: 프로젝트 라이브러리의 알려진 취약점을 정기적으로 확인하기 위해 OWASP Dependency-Check나 Snyk 같은 도구를 CI/CD 파이프라인에 통합하는 것이 좋습니다.

## 📦 주요 의존성

- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-data-redis`
- `spring-boot-starter-oauth2-client`
- `spring-boot-starter-validation`
- `jjwt` (Java JWT)
- `postgresql` (PostgreSQL JDBC Driver)
- `springdoc-openapi-starter-webmvc-ui` (API Documentation)
- `lombok`