# 06. 시스템 아키텍처 (System Architecture)

본 문서는 CafePilot의 전체 시스템 구조, 기술 선택 근거, 핵심 아키텍처 패턴을 정의한다.

---

## 1. 전체 시스템 구성도

```
┌──────────────────────────────────────────────────────────┐
│                        Client                             │
│         (Swagger UI / External API Consumer)              │
└─────────────────────────┬────────────────────────────────┘
                          │ HTTPS
                          ▼
┌──────────────────────────────────────────────────────────┐
│                  Spring Boot Application                   │
│                                                            │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────┐  │
│  │  Controller  │  │   Service    │  │   Repository    │  │
│  │  (API Layer) │→ │(Business     │→ │(Data Access     │  │
│  │             │  │  Logic)      │  │  Layer)         │  │
│  └─────────────┘  └──────┬───────┘  └────────┬────────┘  │
│                          │                   │            │
│                    ┌─────▼─────┐      ┌──────▼──────┐    │
│                    │  RabbitMQ │      │  PostgreSQL  │    │
│                    │ Publisher │      │   (JPA)      │    │
│                    └─────┬─────┘      └─────────────┘    │
│                          │                                │
│                    ┌─────▼──────────────────────────┐    │
│                    │        RabbitMQ Listener        │    │
│                    │  (재고 알림 / 통계 이벤트 처리)   │    │
│                    └────────────────────────────────┘    │
│                                                            │
│  ┌──────────────────────────────────────────────────┐    │
│  │           Spring Security + JWT Filter            │    │
│  └──────────────────────────────────────────────────┘    │
│                                                            │
│  ┌──────────────┐  ┌────────────┐  ┌────────────────┐    │
│  │    Redis     │  │ Scheduler  │  │  AI Service    │    │
│  │  (캐싱)      │  │ (매출 집계) │  │ (LLM 연동)     │    │
│  └──────────────┘  └────────────┘  └────────────────┘    │
└──────────────────────────────────────────────────────────┘
```

---

## 2. 계층형 아키텍처 (Layered Architecture)

CafePilot은 **Controller → Service → Repository** 3계층 구조를 기본으로 한다.

```
┌─────────────────────────────────────────────────────────┐
│  Controller (표현 계층)                                   │
│  - HTTP 요청/응답 처리                                     │
│  - 입력 검증 (@Valid)                                     │
│  - DTO 변환 (Request → Service 호출)                      │
│  - 얇게 유지. 비즈니스 로직 금지                            │
├─────────────────────────────────────────────────────────┤
│  Service (비즈니스 계층)                                   │
│  - 핵심 비즈니스 로직 처리                                  │
│  - 트랜잭션 관리 (@Transactional)                         │
│  - 여러 도메인 객체(애그리거트) 조율                         │
│  - 이벤트 발행                                             │
├─────────────────────────────────────────────────────────┤
│  Domain Entity (도메인 계층)                               │
│  - 불변조건 보호 (Rich Domain Model)                       │
│  - 단일 엔티티의 상태 전이 로직 포함                         │
│  - ex) Order.changeStatus(), Inventory.decrease()        │
├─────────────────────────────────────────────────────────┤
│  Repository (데이터 접근 계층)                              │
│  - Spring Data JPA 기반 DB 접근                           │
│  - 비즈니스 로직 금지 (단순 데이터 접근만)                   │
│  - 복잡한 조회는 JPQL 또는 QueryDSL 사용                   │
└─────────────────────────────────────────────────────────┘
```

**DTO와 Entity를 분리하는 이유**

Entity를 직접 API 응답으로 반환하면 다음 문제가 생긴다.

- 내부 구현이 API 계약에 노출되어, Entity 변경이 곧 API 변경이 된다.
- 순환 참조(Lazy Loading) 문제가 JSON 직렬화 시 발생한다.
- 비밀번호 같은 민감 정보가 노출될 위험이 있다.

따라서 **Request DTO → Entity** (입력), **Entity → Response DTO** (출력) 방향으로 명확히 분리한다.

---

## 3. 패키지 구조

도메인 중심 패키지 구조를 사용한다. 계층 중심(controller/, service/, repository/) 구조보다 도메인 중심 구조가 응집도가 높고 팀 확장 시 모듈 분리가 쉽다.

```
src/main/java/com/cafepilot/
├── CafePilotApplication.java
│
├── global/                          # 전역 공통 모듈
│   ├── config/                      # Spring 설정 (Security, RabbitMQ, Redis 등)
│   ├── exception/                   # Global Exception Handler, 공통 예외
│   ├── response/                    # ApiResponse<T> 공통 응답 포맷
│   ├── security/                    # JWT Filter, UserDetailsService
│   └── util/                        # 공통 유틸리티
│
├── domain/                          # 도메인 모듈
│   ├── auth/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── dto/
│   │   └── entity/                  # Member
│   │
│   ├── cafe/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── dto/
│   │   ├── entity/                  # Cafe
│   │   └── repository/
│   │
│   ├── menu/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── dto/
│   │   ├── entity/                  # Menu
│   │   └── repository/
│   │
│   ├── inventory/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── dto/
│   │   ├── entity/                  # Inventory, InventoryTransaction
│   │   ├── repository/
│   │   └── event/                   # 재고 부족 이벤트
│   │
│   ├── order/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── dto/
│   │   ├── entity/                  # Order, OrderItem
│   │   ├── repository/
│   │   └── event/                   # 주문 완료 이벤트
│   │
│   ├── sales/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── dto/
│   │   ├── entity/                  # SalesSummary
│   │   ├── repository/
│   │   └── scheduler/               # 매출 집계 스케줄러
│   │
│   ├── ai/
│   │   ├── controller/
│   │   ├── service/
│   │   └── dto/
│   │
│   └── admin/
│       ├── controller/
│       ├── service/
│       ├── dto/
│       └── entity/                  # OperationLog
│
└── infra/                           # 외부 인프라 연동
    ├── messaging/                   # RabbitMQ 설정, Publisher, Listener
    ├── cache/                       # Redis 설정, CacheService
    └── ai/                          # LLM API 클라이언트
```

---

## 4. 기술 선택 근거

### 4.1 Java 21 + Spring Boot 3

| 항목 | 선택 이유 |
|---|---|
| Java 21 | LTS 버전. Virtual Thread(Project Loom) 지원으로 고성능 IO 처리 가능. Spring Boot 3은 Java 17 이상 필수이므로 최신 LTS인 21 선택. |
| Spring Boot 3 | 국내 엔터프라이즈 백엔드의 사실상 표준. Auto Configuration, 방대한 생태계, Spring Security/Data JPA 통합이 강점. |
| Gradle | Maven 대비 빌드 스크립트 간결성(Kotlin DSL), 증분 빌드 성능 우위. 대규모 멀티모듈 프로젝트에서도 강점. |

### 4.2 Spring Security + JWT

**왜 JWT인가?**

- 세션 방식은 서버 메모리에 세션을 저장하므로 수평 확장(Scale-out) 시 세션 공유 문제가 생긴다. JWT는 Stateless하여 서버 확장이 자유롭다.
- MSA나 여러 서버로 확장할 때 토큰만으로 인증이 가능하다.

**왜 Access + Refresh Token 이중 구조인가?**

- Access Token만 사용하면 탈취 시 만료까지 계속 사용 가능하다. 만료 시간을 짧게(1시간)하고 Refresh Token(7일)으로 재발급하여 탈취 피해를 최소화한다.

**토큰 구성**

| 항목 | 값 |
|---|---|
| Access Token 만료 | 1시간 |
| Refresh Token 만료 | 7일 |
| Refresh Token 저장 | Redis (무효화 지원을 위해 서버에서 관리) |
| 알고리즘 | HS256 |

> Refresh Token을 Redis에 저장하는 이유: 로그아웃이나 계정 탈취 감지 시 서버에서 토큰을 강제 무효화하기 위함이다. 완전한 Stateless JWT라면 무효화가 불가능하다.

### 4.3 Spring Data JPA + PostgreSQL

**왜 JPA인가?**

- 객체 중심 도메인 모델과 테이블 간 임피던스 불일치를 해결한다.
- 기본 CRUD는 JpaRepository로 처리하고, 복잡한 조회는 JPQL/QueryDSL을 사용한다.
- 다만 JPA가 적합하지 않은 대량 배치 처리(매출 집계)는 직접 JPQL 또는 네이티브 쿼리를 사용한다.

**왜 PostgreSQL인가?**

- MySQL 대비 타입 안정성, 고급 쿼리(윈도우 함수, CTE), JSON 지원이 뛰어나다.
- AWS RDS, Supabase 등 클라우드 환경에서 PostgreSQL 채택이 증가하는 추세다.
- ACID 트랜잭션, 인덱스, 외래키 등 관계형 모델의 장점을 모두 갖추고 있다.
- 카페 운영 데이터는 정형화된 관계(카페-메뉴-주문)를 가지므로 NoSQL보다 RDBMS가 적합하다.
- ENUM 타입은 네이티브 지원 대신 `VARCHAR` + JPA `@Enumerated(EnumType.STRING)`으로 처리하여 DDL 변경 없이 값 추가가 가능하다.

### 4.4 Redis

**사용 목적 2가지**

1. **Refresh Token 저장소**: TTL 기반 자동 만료, 빠른 키-값 조회
2. **매출 통계 캐싱**: 자주 조회되는 일별/월별 통계를 캐싱하여 DB 부하 감소

**왜 Redis인가?**

- 인메모리 구조로 캐시 읽기 속도가 마이크로초 수준이다.
- TTL(Time To Live) 설정이 간편하여 토큰 만료 관리에 최적이다.
- Spring Data Redis, Spring Cache Abstraction(`@Cacheable`)과 통합이 쉽다.

**캐싱 전략**

| 대상 | 전략 | TTL |
|---|---|---|
| 일별 매출 통계 | Cache-Aside (조회 시 캐싱) | 당일 자정까지 |
| 월별 매출 통계 | Cache-Aside | 1시간 |
| Refresh Token | Write-Through | 7일 |

### 4.5 RabbitMQ

**왜 비동기 이벤트가 필요한가?**

주문 생성 API는 다음 작업들을 처리해야 한다.

1. 주문 저장
2. 재고 차감
3. 재고 부족 여부 확인 및 알림
4. 통계 집계 트리거

3, 4번을 동기로 처리하면 주문 API 응답이 느려지고, 알림 발송 실패가 주문 생성 자체를 실패시킬 수 있다. 핵심 트랜잭션(1, 2)과 부가 작업(3, 4)을 분리하기 위해 RabbitMQ 기반 비동기 이벤트 처리를 사용한다.

**이벤트 설계**

| 이벤트 | 발행 시점 | 구독자 |
|---|---|---|
| `order.completed` | 주문 상태 → COMPLETED | 매출 집계 트리거 |
| `inventory.low_stock` | 재고 ≤ threshold | 알림 처리 (로그 기록 / 향후 Push 알림) |

**왜 Kafka가 아닌 RabbitMQ인가?**

- Kafka는 대용량 스트리밍, 이벤트 소싱에 강점이 있으나 운영 복잡도가 높다(Zookeeper 또는 KRaft).
- RabbitMQ는 메시지 브로커 용도로 충분하고, Docker Compose 기반 로컬 개발 환경 구성이 간단하다.
- 이 프로젝트의 이벤트 규모(주문 완료, 재고 부족)는 Kafka가 필요한 수준이 아니다.

### 4.6 동시성 제어 — 낙관적 락 (Optimistic Lock)

**문제**: 동시에 여러 주문이 들어올 때 같은 재고를 동시에 차감하면 race condition이 발생한다.

**해결**: JPA `@Version`을 사용한 낙관적 락

```
Thread A: 재고 조회 (quantity=10, version=1)
Thread B: 재고 조회 (quantity=10, version=1)
Thread A: 차감 → UPDATE inventory SET quantity=8, version=2 WHERE id=1 AND version=1  ✓
Thread B: 차감 → UPDATE inventory SET quantity=8, version=2 WHERE id=1 AND version=1  ✗ (버전 불일치 → OptimisticLockException)
Thread B: 재시도 → 재조회 후 차감 → quantity=6
```

**왜 비관적 락(SELECT FOR UPDATE)을 쓰지 않는가?**

- 비관적 락은 트랜잭션이 길어질수록 대기 스레드가 늘어나 처리량(Throughput)이 떨어진다.
- 낙관적 락은 충돌이 적을 때(카페 1개의 동시 주문이 극단적으로 많지 않은 경우) 더 높은 처리량을 제공한다.
- 충돌 빈도가 높은 환경(대형 카페 피크타임)에서는 비관적 락으로 전환을 검토한다.

### 4.7 AI 연동 전략

**설계 원칙**: AI는 외부 LLM API(OpenAI GPT 등)를 활용하며, 직접 DB 데이터를 근거로 프롬프트를 구성한다.

**동작 방식**

```
1. 사용자 질문 수신 (POST /cafes/{cafeId}/ai/query)
2. 질문의 의도 분류 (매출/재고/인기메뉴/발주추천/추세)
3. 의도에 따라 해당 카페의 DB 데이터 조회
   - 매출 요약 → SalesSummary 조회
   - 재고 부족 → Inventory 조회 (quantity ≤ threshold)
   - 인기 메뉴 → OrderItem 집계 쿼리
4. 조회 데이터를 컨텍스트(Context)로 LLM 프롬프트 구성
5. LLM API 호출 및 자연어 응답 수신
6. 응답 + 근거 데이터(dataContext) 함께 반환
```

**왜 LLM에 직접 DB 접근을 허용하지 않는가?**

- 보안: DB 접근 권한을 LLM에 부여하면 Prompt Injection 공격으로 의도치 않은 쿼리가 실행될 수 있다.
- 제어: 어떤 데이터를 어떻게 조회할지를 백엔드 코드에서 명시적으로 제어한다.
- 성능: DB 결과를 먼저 조회하여 필요한 데이터만 LLM에 전달한다.

---

## 5. 인증/인가 흐름

```
Client
  │
  │  Authorization: Bearer {accessToken}
  ▼
JwtAuthenticationFilter (OncePerRequestFilter)
  │
  ├── 토큰 추출 및 유효성 검증
  │     ├── 유효하지 않음 → 401 반환
  │     └── 유효함 → SecurityContext에 Authentication 설정
  │
  ▼
Spring Security Authorization
  │
  ├── 권한 검증 (@PreAuthorize, SecurityConfig)
  │     ├── 권한 없음 → 403 반환
  │     └── 권한 있음 → Controller 진입
  │
  ▼
Controller → Service
  │
  └── cafeId 소속 검증 (Service 레이어에서 직접 수행)
        ├── 본인 카페가 아님 → 403 CAFE_ACCESS_DENIED
        └── 통과 → 비즈니스 로직 처리
```

---

## 6. 이벤트 처리 흐름

```
OrderService.completeOrder()
  │
  ├── [DB Transaction]
  │     ├── Order 상태 → COMPLETED
  │     └── SalesSummary 집계 트리거용 이벤트 발행 준비
  │
  ├── RabbitMQ Publisher → order.completed 큐에 메시지 발행
  │
  └── 응답 반환 (클라이언트는 이벤트 처리를 기다리지 않음)

RabbitMQ Listener (별도 스레드)
  │
  ├── order.completed 수신
  │     └── SalesSummaryService.aggregateByOrder() 호출
  │
  └── 처리 실패 시
        └── Dead Letter Queue(DLQ)로 이동 + 실패 로그 기록
```

```
InventoryService.decrease()
  │
  ├── [DB Transaction]
  │     └── quantity 차감, InventoryTransaction 기록
  │
  ├── threshold 이하 여부 확인
  │     └── threshold 이하 → RabbitMQ에 inventory.low_stock 이벤트 발행
  │
  └── 응답 반환

RabbitMQ Listener
  │
  └── inventory.low_stock 수신
        └── OperationLog 기록 (향후 Push 알림 확장 포인트)
```

---

## 7. 스케줄러 설계

| 스케줄 | 주기 | 작업 |
|---|---|---|
| 일별 매출 집계 | 매일 새벽 1시 | 전일 완료 주문 기준으로 SalesSummary 생성/갱신 |
| 재고 부족 점검 | 매일 오전 9시 | threshold 이하 재고 일괄 확인 및 이벤트 발행 |

> 스케줄러는 `@Scheduled` + `@EnableScheduling`으로 구현한다.
> 분산 환경에서 중복 실행 방지가 필요하다면 ShedLock 라이브러리 도입을 검토한다(MVP에서는 단일 인스턴스로 운영).

---

## 8. 예외 처리 전략

모든 예외는 `GlobalExceptionHandler` (`@RestControllerAdvice`)에서 일괄 처리하여 공통 응답 포맷(`ApiResponse`)으로 반환한다.

```
BusinessException (커스텀 예외 최상위)
  ├── AuthException       (AUTH_ 코드)
  ├── CafeException       (CAFE_ 코드)
  ├── MenuException       (MENU_ 코드)
  ├── OrderException      (ORDER_ 코드)
  └── InventoryException  (INV_ 코드)
```

**설계 원칙**

- 모든 비즈니스 예외는 `BusinessException`을 상속한다.
- 각 예외는 `ErrorCode` Enum을 가지며, HTTP 상태코드와 에러 메시지를 포함한다.
- Controller는 예외를 직접 처리하지 않는다. Service에서 throw하면 `GlobalExceptionHandler`가 처리한다.

---

## 9. 공통 응답 포맷 구조

```java
ApiResponse<T> {
    boolean success
    T data           // 성공 시 페이로드
    ErrorDetail error // 실패 시 에러 정보
    LocalDateTime timestamp
}

ErrorDetail {
    String code    // 에러 코드 (예: MENU_NOT_FOUND)
    String message // 사람이 읽을 수 있는 메시지
}
```

---

## 10. Docker Compose 구성

```yaml
services:
  app:          # Spring Boot Application
  postgres:     # PostgreSQL 16
  redis:        # Redis 7
  rabbitmq:     # RabbitMQ 3 (Management UI 포함)
```

모든 서비스는 단일 `docker-compose.yml`로 기동된다.
환경변수는 `.env` 파일로 관리하며, `.env`는 `.gitignore`에 포함한다.

---

## 11. 면접 대비 — 기술 선택 질문 예상 답변

**Q. JWT vs Session 방식 중 왜 JWT를 선택했는가?**

> Session 방식은 서버 메모리에 상태를 저장하므로 서버가 여러 대로 확장될 때 세션 공유 문제가 생깁니다. JWT는 Stateless하여 서버 확장이 자유롭습니다. 다만 JWT의 단점(탈취 시 무효화 어려움)을 보완하기 위해 Access Token 만료를 짧게(1시간), Refresh Token을 Redis에 저장하여 필요 시 서버에서 무효화할 수 있게 설계했습니다.

**Q. RabbitMQ를 왜 사용했는가? Kafka와의 차이는?**

> Kafka는 대용량 스트리밍과 이벤트 소싱에 강점이 있지만 운영 복잡도가 높습니다. 이 프로젝트의 이벤트는 주문 완료, 재고 부족 알림 정도로 규모가 크지 않아 RabbitMQ로 충분합니다. RabbitMQ는 메시지 브로커로서 라우팅, 재시도, Dead Letter Queue 기능을 갖추고 있으며, Docker Compose 구성도 단순합니다.

**Q. 재고 동시성 이슈를 어떻게 해결했는가?**

> JPA `@Version` 기반 낙관적 락을 사용했습니다. 동시에 두 요청이 같은 재고를 조회하고 차감 시도할 때, 먼저 커밋된 쪽만 성공하고 나머지는 `OptimisticLockException`이 발생합니다. 이 경우 재조회 후 재시도합니다. 충돌이 적을 것으로 예상되는 카페 환경에서는 비관적 락보다 처리량이 높습니다. 충돌 빈도가 높은 환경이라면 `SELECT FOR UPDATE` 기반 비관적 락으로 전환하겠습니다.

**Q. Redis를 캐싱에만 사용하는가?**

> 두 가지 목적으로 사용합니다. 첫째, Refresh Token 저장소입니다. TTL 기반 자동 만료와 서버 측 무효화를 위해 씁니다. 둘째, 매출 통계 캐싱입니다. 일별/월별 통계는 변경 빈도가 낮고 조회 빈도가 높아 Redis에 캐싱하여 DB 부하를 줄입니다. `@Cacheable` 애노테이션으로 적용합니다.

**Q. 왜 모든 삭제를 Soft Delete로 처리했는가?**

> 메뉴를 물리 삭제하면 과거 주문의 `menu_id`가 가리키는 레코드가 사라져 참조 무결성이 깨집니다. 또한 매출 통계에서 삭제된 메뉴의 이력도 조회 가능해야 합니다. 메뉴는 `status = DELETED`, 회원/카페는 `deleted_at` 컬럼 방식으로 Soft Delete를 적용하고, JPA `@SQLRestriction`으로 삭제된 데이터를 자동 제외합니다.

**Q. MySQL이 아닌 PostgreSQL을 선택한 이유는?**

> PostgreSQL은 MySQL 대비 타입 안정성, 윈도우 함수, CTE, JSON 지원 등 고급 쿼리 기능이 풍부합니다. 매출 집계처럼 복잡한 분석 쿼리가 필요한 이 프로젝트에 더 적합하다고 판단했습니다. 또한 AWS RDS, Supabase 등 최근 클라우드 환경에서 PostgreSQL 채택이 늘어나는 추세이며, ENUM 타입을 `VARCHAR` + 애플리케이션 레벨 검증으로 처리하면 DDL 변경 없이 값을 추가할 수 있어 유지보수성도 높습니다.
