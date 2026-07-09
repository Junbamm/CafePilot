# 07. 개발 로드맵 (Development Roadmap)

본 문서는 CafePilot MVP의 개발 순서와 각 단계별 목표를 정의한다.

설계 문서(01~06)를 기반으로 "무엇을 먼저 만들 것인가"를 결정하는 기준 문서다.

---

## 1. 개발 원칙

- **기능부터 개발하지 않는다.** 설계가 끝난 범위 내에서만 구현한다.
- **수직 슬라이스(Vertical Slice)** 방식으로 진행한다. 한 도메인을 Controller → Service → Repository → Test까지 완성한 뒤 다음 도메인으로 넘어간다. 수평 슬라이스(모든 Controller 먼저, 그다음 모든 Service)는 중간에 동작 확인이 어렵다.
- **각 단계가 끝나면 테스트가 통과해야 다음 단계로 넘어간다.**
- **커밋 단위는 작게 유지한다.** 기능 하나가 완성될 때마다 커밋한다.

---

## 2. 전체 로드맵

| 단계 | 작업 내용 | 핵심 결과물 |
|---|---|---|
| Phase 0 | 프로젝트 초기 세팅 | 실행 가능한 Spring Boot 앱, Docker Compose |
| Phase 1 | 공통 기반 구조 | 공통 응답, 예외 처리, 보안 설정 |
| Phase 2 | 회원 / 인증 | 회원가입, 로그인, JWT 발급 |
| Phase 3 | 카페 관리 | 카페 등록/조회/수정 |
| Phase 4 | 메뉴 + 재고 | 메뉴 CRUD, 재고 1:1 생성 |
| Phase 5 | 주문 | 주문 생성(재고 차감), 상태 변경 |
| Phase 6 | 이벤트 처리 | RabbitMQ 연동, 재고 부족 이벤트 |
| Phase 7 | 매출 통계 | 일별/월별 집계, 인기 메뉴, Redis 캐싱 |
| Phase 8 | AI 질의 | LLM 연동, 데이터 기반 응답 |
| Phase 9 | 관리자 기능 | 사용자 관리, 운영 로그, 통계 |
| Phase 10 | 테스트 보강 | 통합 테스트, 엣지 케이스 |
| Phase 11 | Docker / CI/CD | 도커라이징, GitHub Actions |
| Phase 12 | 리팩토링 / 문서화 | 코드 정리, Swagger, README |

---

## 3. 단계별 상세

### Phase 0. 프로젝트 초기 세팅

**목표**: 모든 인프라가 기동되고 Spring Boot 앱이 실행되는 상태

**작업 목록**
- [ ] Spring Initializr로 프로젝트 생성
  - Java 21, Spring Boot 3, Gradle
  - 의존성: Web, Security, JPA, MySQL Driver, Validation, Redis, RabbitMQ, Lombok
- [ ] `application.yml` 기본 설정 (DB, Redis, RabbitMQ 연결 정보)
- [ ] `docker-compose.yml` 작성 (MySQL, Redis, RabbitMQ)
- [ ] `BaseEntity` 작성 (`createdAt`, `updatedAt`, `@MappedSuperclass`)
- [ ] 앱 기동 확인 (`/actuator/health` 또는 루트 엔드포인트)

**완료 기준**
- `docker-compose up` 으로 인프라 기동
- `./gradlew bootRun` 으로 앱 실행 성공
- MySQL, Redis, RabbitMQ 연결 성공 로그 확인

---

### Phase 1. 공통 기반 구조

**목표**: 모든 도메인에서 공통으로 사용하는 기반 코드 완성

**작업 목록**
- [ ] `ApiResponse<T>` 공통 응답 래퍼 클래스
- [ ] `ErrorCode` Enum (도메인별 에러 코드 정의)
- [ ] `BusinessException` 및 도메인별 하위 예외 클래스
- [ ] `GlobalExceptionHandler` (`@RestControllerAdvice`)
  - `BusinessException` 처리
  - `MethodArgumentNotValidException` 처리 (Validation 실패)
  - 그 외 서버 오류 처리
- [ ] Swagger(OpenAPI) 기본 설정

**완료 기준**
- 잘못된 요청 시 `ApiResponse` 형태의 에러 응답 반환 확인
- Swagger UI 접근 가능 (`/swagger-ui.html`)

---

### Phase 2. 회원 / 인증

**목표**: 회원가입, 로그인, JWT 발급/검증 완성

**작업 목록**
- [ ] `Member` Entity, Repository
- [ ] `MemberService` — 회원가입 (이메일 중복 검증, BCrypt 해싱)
- [ ] `AuthService` — 로그인, JWT Access/Refresh Token 발급
- [ ] `JwtProvider` — 토큰 생성/검증/파싱
- [ ] `JwtAuthenticationFilter` — 요청마다 토큰 검증, SecurityContext 설정
- [ ] `SecurityConfig` — permitAll / authenticated / 역할별 URL 접근 설정
- [ ] Refresh Token Redis 저장/조회/삭제
- [ ] `POST /api/v1/auth/register` 구현
- [ ] `POST /api/v1/auth/login` 구현
- [ ] `POST /api/v1/auth/refresh` 구현
- [ ] `GET /api/v1/members/me` 구현
- [ ] 단위 테스트: `MemberService`, `AuthService`, `JwtProvider`
- [ ] 통합 테스트: 회원가입 → 로그인 → 보호 API 접근 흐름

**완료 기준**
- 회원가입 후 로그인 시 JWT 발급
- JWT 없이 보호 API 접근 시 401 반환
- 잘못된 역할로 접근 시 403 반환

---

### Phase 3. 카페 관리

**목표**: 카페 등록/조회/수정 + cafeId 기반 소속 검증

**작업 목록**
- [ ] `Cafe` Entity, Repository
- [ ] `CafeService` — 등록, 조회, 수정, 소속 검증 로직
- [ ] cafeId 소속 검증 공통 처리 (서비스 레이어에서 수행)
- [ ] `POST /api/v1/cafes` 구현
- [ ] `GET /api/v1/cafes` 구현
- [ ] `GET /api/v1/cafes/{cafeId}` 구현
- [ ] `PATCH /api/v1/cafes/{cafeId}` 구현
- [ ] 단위 테스트: `CafeService`
- [ ] 통합 테스트: 카페 등록 → 조회 → 수정 흐름

**완료 기준**
- OWNER만 카페 등록 가능
- 타인 카페 접근 시 `CAFE_ACCESS_DENIED` 반환

---

### Phase 4. 메뉴 + 재고

**목표**: 메뉴 CRUD + 메뉴 등록 시 재고 1:1 자동 생성

**작업 목록**
- [ ] `Menu` Entity, Repository
- [ ] `Inventory`, `InventoryTransaction` Entity, Repository
- [ ] `MenuService` — CRUD, Soft Delete (`DELETED` 상태 처리)
- [ ] `InventoryService` — 재고 조회, 수동 보충
- [ ] 메뉴 등록 시 `Inventory` 함께 생성 트랜잭션 처리
- [ ] `POST /api/v1/cafes/{cafeId}/menus` 구현
- [ ] `GET /api/v1/cafes/{cafeId}/menus` 구현
- [ ] `PATCH /api/v1/cafes/{cafeId}/menus/{menuId}` 구현
- [ ] `DELETE /api/v1/cafes/{cafeId}/menus/{menuId}` 구현
- [ ] `GET /api/v1/cafes/{cafeId}/inventories` 구현
- [ ] `PATCH /api/v1/cafes/{cafeId}/inventories/{inventoryId}/restock` 구현
- [ ] 단위 테스트: `MenuService`, `InventoryService`

**완료 기준**
- 메뉴 등록 시 재고 레코드 자동 생성 확인
- 메뉴 삭제 시 `status = DELETED` 처리 (물리 삭제 아님)

---

### Phase 5. 주문

**목표**: 주문 생성(재고 차감, 낙관적 락), 상태 전이

**작업 목록**
- [ ] `Order`, `OrderItem` Entity, Repository
- [ ] `Inventory.decrease()` 엔티티 메서드 — 재고 차감 + 음수 방지
- [ ] `Order.changeStatus()` 엔티티 메서드 — 상태 전이 규칙 검증
- [ ] `Inventory`에 `@Version` 낙관적 락 적용
- [ ] `OrderService.createOrder()` — 재고 확인 → 주문 생성 → 재고 차감 트랜잭션
- [ ] `OrderService.changeStatus()` — 상태 전이 처리
- [ ] `POST /api/v1/cafes/{cafeId}/orders` 구현
- [ ] `GET /api/v1/cafes/{cafeId}/orders` 구현 (페이지네이션)
- [ ] `GET /api/v1/cafes/{cafeId}/orders/{orderId}` 구현
- [ ] `PATCH /api/v1/cafes/{cafeId}/orders/{orderId}/status` 구현
- [ ] 단위 테스트: `OrderService`, `Inventory.decrease()`, `Order.changeStatus()`
- [ ] 동시성 테스트: 동시 주문 시 재고 정합성 검증

**완료 기준**
- 재고 부족 시 `INV_INSUFFICIENT` 반환
- 동시 주문에서 재고가 음수로 내려가지 않음
- 잘못된 상태 전이 시 `ORDER_INVALID_STATUS` 반환

---

### Phase 6. 이벤트 처리 (RabbitMQ)

**목표**: 주문 완료 / 재고 부족 비동기 이벤트 처리

**작업 목록**
- [ ] RabbitMQ Exchange, Queue, Binding 설정
- [ ] `OrderCompletedEvent` 발행 — 주문 상태 → COMPLETED 시
- [ ] `InventoryLowStockEvent` 발행 — 재고 ≤ threshold 시
- [ ] `OrderEventListener` — `order.completed` 수신 → 통계 집계 트리거
- [ ] `InventoryEventListener` — `inventory.low_stock` 수신 → OperationLog 기록
- [ ] Dead Letter Queue(DLQ) 설정
- [ ] 이벤트 처리 실패 시 로그 기록

**완료 기준**
- 주문 완료 이벤트 발행 후 리스너 정상 수신 확인
- 재고 부족 이벤트 발행 및 로그 기록 확인
- RabbitMQ Management UI에서 큐/메시지 흐름 확인

---

### Phase 7. 매출 통계 + Redis 캐싱

**목표**: 일별/월별 매출 집계, 인기 메뉴, Redis 캐싱 적용

**작업 목록**
- [ ] `SalesSummary` Entity, Repository
- [ ] `SalesSummaryService` — 일별 집계 로직 (완료 주문 기준)
- [ ] `@Scheduled` 스케줄러 — 매일 새벽 1시 집계 실행
- [ ] `GET /api/v1/cafes/{cafeId}/sales/daily` 구현
- [ ] `GET /api/v1/cafes/{cafeId}/sales/monthly` 구현
- [ ] `GET /api/v1/cafes/{cafeId}/sales/top-menus` 구현
- [ ] Redis `@Cacheable` 적용 (일별/월별 통계)
- [ ] 캐시 무효화 전략 — 새 집계 생성 시 해당 날짜 캐시 삭제
- [ ] 단위 테스트: `SalesSummaryService`

**완료 기준**
- 스케줄러 실행 후 `SalesSummary` 레코드 생성 확인
- 동일 통계 반복 조회 시 Redis에서 응답 (DB 쿼리 미발생)

---

### Phase 8. AI 질의응답

**목표**: 실제 DB 데이터를 컨텍스트로 LLM 호출

**작업 목록**
- [ ] LLM API 클라이언트 설정 (OpenAI 또는 대안 API)
- [ ] 질문 의도 분류 로직 (키워드 기반 or LLM 분류)
- [ ] 의도별 데이터 조회 서비스
  - 매출 요약 → `SalesSummary` 조회
  - 재고 부족 → `Inventory` 조회
  - 인기 메뉴 → `OrderItem` 집계
  - 발주 추천 → 재고 + 판매 추세 조합
- [ ] 프롬프트 빌더 — 조회 데이터를 자연어 컨텍스트로 변환
- [ ] `POST /api/v1/cafes/{cafeId}/ai/query` 구현
- [ ] AI API Key 환경변수 처리 (`.env`, `application.yml` 분리)

**완료 기준**
- 실제 DB 데이터를 근거로 한 AI 응답 반환
- API Key가 코드/커밋에 포함되지 않음

---

### Phase 9. 관리자 기능

**목표**: ADMIN 전용 사용자 관리, 운영 로그, 통계

**작업 목록**
- [ ] `OperationLog` Entity, Repository
- [ ] 주요 작업(주문 상태 변경, 회원 비활성화 등) 시 `OperationLog` 기록
- [ ] `GET /api/v1/admin/members` 구현
- [ ] `PATCH /api/v1/admin/members/{memberId}/status` 구현
- [ ] `GET /api/v1/admin/operation-logs` 구현
- [ ] `GET /api/v1/admin/stats` 구현

**완료 기준**
- ADMIN이 아닌 사용자 접근 시 403 반환
- 주요 작업 수행 후 OperationLog 기록 확인

---

### Phase 10. 테스트 보강

**목표**: 핵심 비즈니스 로직 테스트 커버리지 확보

**작업 목록**
- [ ] 단위 테스트 누락 케이스 보완
  - 재고 차감 음수 방지
  - 주문 상태 전이 규칙
  - 이메일 중복 가입 거부
- [ ] 통합 테스트 (주요 흐름)
  - 회원가입 → 로그인 → 카페 등록 → 메뉴 등록 → 주문 생성 → 완료 흐름
  - 재고 부족 주문 거부 흐름
- [ ] 테스트 환경 설정 (`H2` 또는 `TestContainers`)

---

### Phase 11. Docker / CI/CD

**목표**: 도커라이징 및 자동화 파이프라인 구성

**작업 목록**
- [ ] `Dockerfile` 작성 (멀티 스테이지 빌드)
- [ ] `docker-compose.yml` 앱 서비스 추가 (MySQL, Redis, RabbitMQ 포함)
- [ ] `.env.example` 작성 (환경변수 템플릿)
- [ ] GitHub Actions 워크플로우
  - PR 시: 빌드 + 테스트 자동 실행
  - main 머지 시: Docker 이미지 빌드

**완료 기준**
- `docker-compose up` 하나로 전체 시스템 기동
- PR 생성 시 CI 자동 실행 및 테스트 통과 확인

---

### Phase 12. 리팩토링 / 문서화

**목표**: 코드 품질 정리 및 포트폴리오 완성

**작업 목록**
- [ ] Swagger 어노테이션 보강 (`@Operation`, `@Tag`, `@ApiResponse`)
- [ ] `README.md` 작성 (프로젝트 소개, 실행 방법, 아키텍처 요약)
- [ ] `08-troubleshooting.md` 작성 (개발 중 마주한 이슈 및 해결 과정)
- [ ] 코드 리뷰 — 불필요한 로직 제거, 네이밍 정리
- [ ] 로깅 점검 — 민감 정보 로그 출력 여부 확인
- [ ] `.env` 파일이 `.gitignore`에 포함되어 있는지 최종 확인

---

## 4. 현재 진행 상태

| 단계 | 상태 |
|---|---|
| Phase 0 | 대기 중 |
| Phase 1 | 대기 중 |
| Phase 2 | 대기 중 |
| Phase 3 | 대기 중 |
| Phase 4 | 대기 중 |
| Phase 5 | 대기 중 |
| Phase 6 | 대기 중 |
| Phase 7 | 대기 중 |
| Phase 8 | 대기 중 |
| Phase 9 | 대기 중 |
| Phase 10 | 대기 중 |
| Phase 11 | 대기 중 |
| Phase 12 | 대기 중 |

> 단계 완료 시 상태를 `완료`로 업데이트한다.

---

## 5. 다음 단계

설계 문서(01~07)가 완성되면 **Phase 0. 프로젝트 초기 세팅**부터 구현을 시작한다.
