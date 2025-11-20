# Vote Backend API 명세서

## 개요
- **Base URL**: `https://<host>` (모든 경로는 이 기본 URL 뒤에 붙습니다.)
- **표준 헤더**: `Content-Type: application/json`
- **인증 방식**
  - 자체 로그인 이후 발급된 `Authorization: Bearer <accessToken>` 헤더를 사용합니다.
  - 장기 인증은 HTTP-only 쿠키 `refresh_token`으로 관리되며, `/jwt/*` 및 `/v1/user/login` 응답에서 회전(rotating)됩니다.
- **오류 처리**: Spring Validation 및 예외 처리에 따라 4xx/5xx 상태 코드와 메시지가 반환됩니다. 대표적인 에러 사유를 각 섹션에 정리했습니다.

---

## JWT 관련 API

| 메서드 | 경로 | 설명 | 인증 |
| ------ | ---- | ---- | ---- |
| POST | `/jwt/exchange` | 소셜 로그인 직후 쿠키에 담긴 Refresh 토큰을 읽어 Access 토큰으로 교환 | `refresh_token` 쿠키 필요 |
| POST | `/jwt/refresh` | Refresh 토큰으로 Access 토큰 재발급 (회전 포함) | `refresh_token` 쿠키 필요 |

### POST `/jwt/exchange`
- **설명**: 소셜 로그인 성공 후 프론트엔드가 쿠키 기반 Refresh 토큰을 서버에 전송하면, 서버가 Access 토큰을 JSON 헤더로 응답하고 새로운 Refresh 토큰을 쿠키로 재설정합니다.
- **요청**
  - 헤더: `Content-Type: application/json`
  - 쿠키: `refresh_token=<기존Refresh>`
  - 바디: 없음
- **성공 응답**
  - 상태 코드: `200 OK`
  - Body:
    ```json
    {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR..."
    }
    ```
  - 쿠키: `Set-Cookie: refresh_token=<신규Refresh>; HttpOnly; Path=/; Max-Age=604800`
- **주요 오류**
  - 400: 쿠키 누락 또는 비어 있는 Refresh 토큰
  - 401: 만료/위조 등으로 `JWTUtil.isValid()` 검증 실패
  - 404: 화이트리스트(`RefreshRepository`)에 토큰이 존재하지 않음

### POST `/jwt/refresh`
- **설명**: 자체 로그인 사용자가 Access 토큰을 갱신할 때 사용합니다. 쿠키 이름은 `refresh_token` 혹은 `refreshToken` 모두 허용됩니다.
- **요청**
  - 헤더: `Content-Type: application/json`
  - 쿠키: `refresh_token=<기존Refresh>` (또는 `refreshToken`)
  - 바디: 없음 (컨트롤러가 쿠키를 DTO로 변환)
- **성공 응답**
  - 상태 코드: `200 OK`
  - Body 동일: `{"accessToken": "<신규Access>" }`
  - 쿠키: `refresh_token` 재설정 (회전)
- **주요 오류**
  - 400: 쿠키 내 Refresh 토큰 미존재
  - 401: Refresh 토큰 만료/위조
  - 404: 화이트리스트에 토큰이 없음 (타 기기에서 이미 삭제된 경우 등)

---

## 회원 API (`/v1/user`)

| 메서드 | 경로 | 설명 | 인증 |
| ------ | ---- | ---- | ---- |
| POST | `/v1/user/exist` | 자체 회원가입 전 아이디 존재 여부 확인 | 불필요 |
| POST | `/v1/user` | 회원가입 | 불필요 |
| GET | `/v1/user` | 내 정보 조회 | Access 토큰 |
| PUT | `/v1/user` | 내 정보 수정 (자체 로그인만) | Access 토큰 |
| DELETE | `/v1/user` | 회원 탈퇴 (본인 또는 관리자) | Access 토큰 |
| PATCH | `/v1/user/nickname` | 소셜 로그인 사용자의 닉네임 등록/수정 | Access 토큰 |
| POST | `/v1/user/login` | 자체 로그인 | 불필요 (응답 시 Access 토큰 + Refresh 쿠키 발급) |

### POST `/v1/user/exist`
- **설명**: 입력한 `username`(최소 4자)이 이미 존재하는지 Boolean으로 반환합니다.
- **요청 바디**

  | 필드 | 타입 | 필수 | 제약 | 설명 |
  | ---- | ---- | ---- | ---- | ---- |
  | `username` | string | O | 최소 4자 | 중복 체크 대상 ID |

- **응답**: `200 OK`, Body 예시 `true`

### POST `/v1/user`
- **설명**: 새로운 자체 로그인 사용자를 생성합니다.
- **요청 바디**

  | 필드 | 타입 | 필수 | 제약 | 설명 |
  | ---- | ---- | ---- | ---- | ---- |
  | `username` | string | O | 최소 4자 | 로그인 ID |
  | `password` | string | O | 최소 4자 | 로그인 비밀번호 |
  | `nickname` | string | O | - | 표시 이름 |
  | `email` | string | O | 이메일 형식 | 연락처 이메일 |

- **성공 응답**
  - 상태 코드: `201 Created`
  - Body:
    ```json
    {
      "username": "vote_user",
      "isSocial": false,
      "nickname": "투표왕",
      "email": "vote@example.com",
      "needsNickname": false
    }
    ```
- **주요 오류**: 400 (Validation 실패), 409 (이미 존재하는 사용자일 때 `IllegalArgumentException`)

### GET `/v1/user`
- **설명**: 현재 인증된 사용자의 프로필을 반환합니다.
- **요청**: `Authorization: Bearer <accessToken>`
- **응답**: `200 OK` + `UserResponseDTO` 구조(위 예시와 동일)
- **오류**: 401 (인증 토큰 누락/만료), 404 (사용자 미존재)

### PUT `/v1/user`
- **설명**: 자체 로그인 사용자가 자신의 닉네임/이메일을 수정합니다. `username`이 세션 ID와 다르면 403이 발생합니다.
- **요청 바디**

  | 필드 | 타입 | 필수 | 제약 |
  | ---- | ---- | ---- | ---- |
  | `username` | string | O | 최소 4자, 세션 사용자와 동일해야 함 |
  | `nickname` | string | O | - |
  | `email` | string | O | 이메일 형식 |

- **응답**: `200 OK`, Body: 수정된 사용자 PK (`Long`)
- **오류**: 400 (검증 실패), 401/403 (타 사용자가 호출), 404 (사용자 미존재)

### DELETE `/v1/user`
- **설명**: Refresh 토큰을 모두 제거한 뒤 사용자를 삭제합니다. 본인 혹은 ROLE_ADMIN만 호출 가능합니다.
- **요청 바디**

  | 필드 | 타입 | 필수 | 설명 |
  | ---- | ---- | ---- | ---- |
  | `username` | string | O | 삭제 대상 ID |

- **응답**: `200 OK`, Body: `true`
- **오류**: 403 (권한 없음)

### PATCH `/v1/user/nickname`
- **설명**: 소셜 로그인 사용자가 닉네임을 별도로 입력/변경합니다. 세션의 사용자명이 자동 주입됩니다.
- **요청 바디**

  | 필드 | 타입 | 필수 | 제약 |
  | ---- | ---- | ---- | ---- |
  | `nickname` | string | O | 2~10자 |

- **응답**: `200 OK`, Body: `UserResponseDTO`
- **오류**: 400 (검증 실패), 401 (미인증), 404 (소셜 사용자 미존재)

### POST `/v1/user/login`
- **설명**: 자체 로그인 전용 엔드포인트입니다. 로그인에 성공하면 Access 토큰을 JSON으로, Refresh 토큰을 쿠키로 발급합니다. 로그인 시 기존 동일 기기(deviceId)의 Refresh 토큰은 삭제 후 재발급됩니다.
- **요청 바디**

  | 필드 | 타입 | 필수 | 설명 |
  | ---- | ---- | ---- | ---- |
  | `username` | string | O | 로그인 ID |
  | `password` | string | O | 로그인 비밀번호 |
  | `deviceId` | string | O | 기기 구분자 (없으면 `"unknown-device"` 처리) |

- **성공 응답**
  - 상태 코드: `200 OK`
  - Body:
    ```json
    {
      "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
      "user": {
        "username": "vote_user",
        "isSocial": false,
        "nickname": "투표왕",
        "email": "vote@example.com",
        "needsNickname": false
      }
    }
    ```
  - 쿠키: `refresh_token=<신규Refresh>; HttpOnly; Secure=false(로컬), Path=/; Max-Age=604800`
- **주요 오류**
  - 400: Validation 실패
  - 401: 아이디 미존재 또는 비밀번호 불일치 (`IllegalArgumentException`)

---

## 부록: DTO 요약

| DTO | 필드 | 설명 |
| --- | ---- | ---- |
| `UserResponseDTO` | `username`, `isSocial`, `nickname`, `email`, `needsNickname` | 사용자 프로필 기본 응답 |
| `AuthLoginResponseDTO` | `accessToken`, `user(UserResponseDTO)` | 로그인 성공 시 반환 구조 |
| `JWTResponseDTO` | `accessToken` | JWT 교환/갱신 응답 |
| `UserRequestDTO` | `username`, `password`, `nickname`, `email` | Validation 그룹에 따라 필수 항목이 다름 |
| `LoginRequestDTO` | `username`, `password`, `deviceId` | 자체 로그인 요청 |
| `NicknameUpdateRequestDTO` | `nickname (2~10자)` | 소셜 닉네임 등록/변경 요청 |

---

## 테스트 시나리오 예시
1. `/v1/user/exist`로 신규 아이디 중복 확인
2. `/v1/user`(POST)로 회원가입
3. `/v1/user/login` 호출 → Access 토큰과 쿠키 확보
4. 보호된 API 호출 시 `Authorization` 헤더에 Access 토큰 추가
5. Access 토큰 만료 시 `/jwt/refresh`로 갱신
6. 소셜 사용자라면 `/v1/user/nickname`으로 닉네임 입력

