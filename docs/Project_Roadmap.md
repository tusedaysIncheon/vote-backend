# 🗺️ Vote Project Development Roadmap

이 문서는 프로젝트의 현재 진행 상황과 향후 개발 계획을 정리한 로드맵입니다.
매일 작업 시작 전 이 문서를 확인하여 진행 상황을 파악하고, Notion 등 외부 툴에 기록하는 용도로 활용하세요.

---

## 📌 1. 현재 진행 상황 (Current Status)

### ✅ Phase 1: User & Auth Foundation (완료)
- [x] **도메인 분리**: `User`와 `Auth` 관심사 분리 (Refactoring)
- [x] **인증 서비스 구현**:
    - `CustomUserDetailsService` (Form Login)
    - `CustomOAuth2UserService` (Social Login: Google, Naver)
- [x] **JWT 보안 설정**: `SecurityConfig`, `JWTFilter` 최적화
- [x] **버그 수정**: 소셜 로그인 유저(`isSocial=true`) JWT 인증 실패 문제 해결

### ✅ Phase 2: Vote Core - Create & Read (완료)
- [x] **투표 생성 (`createVote`)**: 투표 주제 및 옵션 저장, 이미지 처리
- [x] **투표 목록 조회 (`getFeedList`)**:
    - Gravity 알고리즘 기반 정렬
    - **Redis Caching**: `MGET`을 활용한 대량 조회 최적화
    - **Pagination**: 무한 스크롤 지원
- [x] **투표 상세 조회 (`getVoteDetails`)**: 투표 정보 + 실시간 통계(`VoteStats`) 병합
- [x] **투표 하기 (`castVote`)**:
    - **Redis Stream**: 대용량 트래픽 처리를 위한 비동기 아키텍처 적용
    - **Concurrency Control**: 중복 투표 방지 (Redis Set & DB Check)
    - **Consumer**: `VoteEventConsumer`를 통한 DB 비동기 저장

---

## 🚀 2. 다음 개발 목표 (Next Step)

### 🚧 Phase 3: Vote Core - Update & Delete (진행 예정)
현재 투표 기능의 CRUD 중 **UD(수정/삭제)**가 미구현 상태입니다.

#### 📋 3-1. 투표 수정 (`updateVote`)
*   **목표**: 진행 중인 투표의 정보를 안전하게 수정한다.
*   **필요한 작업 (Checklist)**:
    - [ ] **DTO 분리**: `VoteUpdateDTO` 생성 (옵션 수정 불가, 제목/내용/마감기한만 허용)
    - [ ] **유효성 검사**: 마감 기한 연장 시 현재 시간보다 미래인지 검증 (`@Future`)
    - [ ] **권한 체크**: 요청자가 투표 작성자인지 확인 (`Writer` 일치 여부)
    - [ ] **캐시 무효화 (Cache Eviction)**: 수정 시 Redis에 저장된 `vote:info:{id}` 삭제 (Stale Data 방지)

#### 📋 3-2. 투표 삭제 (`deleteVote`)
*   **목표**: 투표와 관련된 모든 데이터(기록, 통계)를 깔끔하게 삭제한다.
*   **필요한 작업 (Checklist)**:
    - [ ] **참조 무결성 처리**: 투표를 지우기 전 `VoteRecord` (누가 어디에 투표했는지) 데이터를 먼저 삭제해야 함.
    - [ ] **DB 삭제**: `VoteEntity` 삭제 (Cascade 설정으로 `VoteOption`은 자동 삭제 유도)
    - [ ] **Redis 정리**:
        - `vote:info:{id}` (투표 정보)
        - `vote:stats:{id}` (투표 통계)
        - `vote:voters:{id}` (참여 유저 목록)
        - `vote:readers:{id}` (조회수 등)

---

## 🔮 3. 향후 고도화 계획 (Future Roadmap)

### 💬 Phase 4: Real-time Chat (WebSocket)
투표 상세 화면에서 유저들이 실시간으로 의견을 나눌 수 있는 채팅 기능을 구현합니다.

*   **핵심 기술**: `Spring WebSocket` + `STOMP`
*   **아키텍처 전략**:
    1.  **Pub/Sub 모델**: Redis Pub/Sub을 활용하여 다중 서버 환경에서도 채팅 메시지 동기화
    2.  **Message Broker**: 내장 브로커 또는 외부 브로커(RabbitMQ/ActiveMQ) 고려
    3.  **Chat Storage**:
        - 단기: Redis List (최근 메시지 캐싱)
        - 장기: MongoDB (채팅 이력 저장) 또는 RDB

### 🔔 Phase 5: Notifications (SSE)
내 투표에 댓글이 달리거나, 참여한 투표가 마감되었을 때 실시간 알림을 보냅니다.

*   **핵심 기술**: `Server-Sent Events (SSE)` (단방향 통신에 적합)
*   **구현 시나리오**:
    1.  **구독(Subscribe)**: 클라이언트가 로그인 시 `/api/v1/notify/subscribe` 연결
    2.  **이벤트 발생(Publish)**:
        - "투표 마감" 스케줄러가 이벤트 발행
        - "댓글 작성" 시 이벤트 발행
    3.  **비동기 전송**: `SseEmitter`를 통해 연결된 유저에게 즉시 전송
    4.  **연결 유실 처리**: `Last-Event-ID` 헤더를 통해 재연결 시 누락된 알림 재전송 로직 구현

---

## 📝 Daily Log (Template)
> 매일 작업 후 아래 양식에 맞춰 진행 상황을 기록하세요.

**날짜**: 2026-02-xx
**오늘의 목표**: (예: 투표 수정 기능 구현 및 테스트)

**진행 사항**:
- [ ] DTO 생성 및 유효성 검사 로직 작성
- [ ] Service 레이어 업데이트 로직 구현
- [ ] Redis 캐시 삭제 테스트

**이슈 및 해결**:
- (예: 캐시가 안 지워져서 RedisConfig 설정을 확인했음)

**내일 할 일**:
-
