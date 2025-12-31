# Vote Backend

이 프로젝트는 Vote Project의 백엔드 서버로, Java와 Spring Boot를 기반으로 구축되었습니다.

## 🚀 주요 기술 스택

- **Framework**: Spring Boot, Spring Security
- **Language**: Java
- **Database**: Spring Data JPA, PostgreSQL
- **Authentication**: JWT (JSON Web Tokens)

## ✨ 주요 기능

### 1. 사용자 인증 및 인가
- **Spring Security 기반 인증**: 강력한 보안 프레임워크인 Spring Security를 통해 인증 및 인가 흐름을 제어합니다.
- **이메일/비밀번호 로그인**: `UsernamePasswordAuthenticationFilter`를 커스텀하여 기본적인 로그인을 처리합니다.
- **OAuth2 소셜 로그인**: Google, Naver, Kakao 등 다양한 OAuth2 제공자를 통한 소셜 로그인을 지원하며, 성공 시 자체 JWT를 발급합니다.
- **JWT 기반 API 인증**: 인증된 사용자는 JWT(Access Token, Refresh Token)를 발급받아 API 요청 시 자신을 증명합니다.
- **JWT 필터**: 모든 요청에 대해 JWT 토큰의 유효성을 검사하여 API를 보호합니다.

### 2. 토큰 관리
- **Access/Refresh Token 발급**: 로그인 성공 시, 역할을 포함한 Access Token과 세션 유지를 위한 Refresh Token을 함께 발급합니다.
- **Refresh Token을 이용한 토큰 재발급**: Access Token이 만료되면 사용자는 Refresh Token을 보내 새로운 Access Token을 재발급받을 수 있어, 로그인을 다시 할 필요가 없습니다.
- **안전한 로그아웃**: 로그아웃 시 서버에 저장된 Refresh Token을 삭제하여 해당 토큰이 더 이상 사용될 수 없도록 처리합니다.

### 3. API 및 데이터 관리
- **RESTful API**: 사용자 정보 조회, 닉네임 변경 등 REST 원칙을 따르는 API 엔드포인트를 제공합니다.
- **데이터베이스 연동**: Spring Data JPA를 사용하여 사용자 정보를 관계형 데이터베이스에 영속적으로 저장하고 관리합니다.
- **엔티티 설계**: 사용자(`UserEntity`), 리프레시 토큰(`RefreshEntity`) 등 도메인 모델을 정의하고 관계를 설정합니다.
- **전역 예외 처리**: `@RestControllerAdvice`를 사용하여 API 처리 중 발생하는 예외를 중앙에서 관리하고 일관된 형식의 에러 응답을 반환합니다.
