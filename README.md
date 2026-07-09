# CafePilot ☕

> 카페 사장을 위한 **AI 기반 운영 지원 SaaS 플랫폼**

[![CI](https://github.com/Junbamm/CafePilot/actions/workflows/ci.yml/badge.svg)](https://github.com/Junbamm/CafePilot/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

---

## 프로젝트 소개

CafePilot은 소규모 카페 사장이 **메뉴 관리, 재고 추적, 주문 처리, 판매 분석**을 하나의 플랫폼에서 처리하고, **AI 추천**으로 운영 인사이트를 얻을 수 있는 백엔드 SaaS입니다.

### 해결하는 문제

| 문제 | 해결 |
|------|------|
| 엑셀/수기 재고 관리 | 실시간 재고 추적 + 부족 알림 |
| 매출 데이터 파편화 | 일별 자동 집계 |
| 운영 인사이트 부재 | AI 기반 메뉴·재고 추천 |

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Security | Spring Security 6 + JWT |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Message Queue | RabbitMQ 3 |
| ORM | Spring Data JPA + QueryDSL 5 |
| AI | OpenAI Chat Completions API |
| Container | Docker + Docker Compose |
| CI | GitHub Actions |
| Docs | Springdoc OpenAPI (Swagger) |

---

## 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                        Client                           │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP/REST
┌────────────────────────▼────────────────────────────────┐
│              Spring Boot Application                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐  │
│  │Controller│→ │ Service  │→ │Repository│→ │  DB    │  │
│  └──────────┘  └────┬─────┘  └──────────┘  └────────┘  │
│                     │                                   │
│              ┌──────▼──────┐  ┌──────────┐             │
│              │  RabbitMQ   │  │  Redis   │             │
│              │  (Events)   │  │  (Cache) │             │
│              └─────────────┘  └──────────┘             │
└─────────────────────────────────────────────────────────┘
```

### 패키지 구조

```
com.cafepilot
├── domain/
│   ├── member/      # 회원
│   ├── auth/        # 인증 (JWT)
│   ├── cafe/        # 카페
│   ├── menu/        # 메뉴
│   ├── inventory/   # 재고
│   ├── order/       # 주문
│   ├── sales/       # 판매 집계
│   └── ai/          # AI 추천
└── global/
    ├── config/      # 설정 (Security, Swagger, Redis, RabbitMQ, OpenAI)
    ├── entity/      # BaseEntity
    ├── exception/   # 공통 예외 처리
    ├── response/    # 공통 응답 형식
    └── security/    # JWT 필터
```

---

## 빠른 시작

### 사전 요구사항

- Docker Desktop
- Java 21
- Gradle 8.x

### 1. 인프라 실행

```bash
cd docker
cp .env.example .env       # 환경변수 설정
docker compose up -d       # PostgreSQL + Redis + RabbitMQ 실행
```

### 2. 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

### 3. API 문서 확인

```
http://localhost:8080/swagger-ui.html
```

### 전체 스택 Docker로 실행

```bash
cd docker
docker compose up -d --build    # 앱 포함 전체 스택 실행
```

---

## API 엔드포인트

| 도메인 | 기본 경로 | 주요 기능 |
|--------|-----------|-----------|
| Auth | `/api/v1/auth` | 회원가입, 로그인, 토큰 재발급, 로그아웃 |
| Member | `/api/v1/members` | 내 정보 조회/수정, 비밀번호 변경, 탈퇴 |
| Cafe | `/api/v1/cafes` | 카페 등록/수정/삭제, 영업 상태 토글 |
| Menu | `/api/v1/cafes/{id}/menus` | 메뉴 CRUD, 판매 상태 토글 |
| Inventory | `/api/v1/cafes/{id}/inventories` | 재고 조회/조정, 부족 재고 알림 |
| Order | `/api/v1/cafes/{id}/orders` | 주문 생성, 상태 관리 (수락/준비/완료/취소) |
| Sales | `/api/v1/cafes/{id}/sales` | 기간별 판매 집계 조회 |
| AI | `/api/v1/cafes/{id}/ai` | AI 운영 추천 |

**인증 방식:** `Authorization: Bearer {accessToken}`

---

## 주요 설계 결정

### 1. JWT 무상태 인증

- Access Token (1시간) + Refresh Token (7일, Redis 저장)
- Refresh Token Rotation: 재발급 시 기존 토큰 폐기
- 로그아웃 시 Redis에서 Refresh Token 삭제

### 2. 낙관적 락(Optimistic Lock)

- `Member`, `Cafe`, `Menu`, `Inventory`, `Order` 엔티티에 `@Version` 적용
- 동시 재고 차감 시 Lost Update 방지

### 3. 소프트 삭제(Soft Delete)

- `deletedAt` 필드로 논리 삭제
- 주문·판매 이력과 연결된 데이터 물리 삭제 방지

### 4. 도메인 간 결합 최소화

- 엔티티 직접 참조 대신 ID(Long) 값 저장
- 예: `Menu.cafeId`, `Order.memberId`

### 5. 비동기 이벤트 (RabbitMQ)

- 주문 생성 시 `order.created` 이벤트 발행
- MQ 장애 시 주문 자체는 성공 처리 (장애 격리)

### 6. 일별 판매 집계 스케줄러

- 매일 새벽 1시 자동 실행 (`@Scheduled`)
- 카페별 독립 집계, 실패 시 다른 카페에 영향 없음

---

## 테스트

```bash
cd backend
./gradlew test
```

| 테스트 종류 | 파일 | 설명 |
|------------|------|------|
| 엔티티 단위 | `MemberTest`, `OrderTest`, `InventoryTest` | 도메인 불변식, 상태 전이 |
| 서비스 단위 | `AuthServiceTest` | Mockito 의존성 격리 |
| 컨트롤러 슬라이스 | `AuthControllerTest` | MockMvc, HTTP 계층 |

---

## 환경 변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `DB_HOST` | PostgreSQL 호스트 | `localhost` |
| `DB_PASSWORD` | DB 비밀번호 | - |
| `JWT_SECRET` | JWT 서명 키 (32자+) | - |
| `OPENAI_API_KEY` | OpenAI API 키 | - |
| `REDIS_HOST` | Redis 호스트 | `localhost` |
| `RABBITMQ_HOST` | RabbitMQ 호스트 | `localhost` |

전체 목록은 `docker/.env.example` 참고

---

## 프로젝트 문서

| 문서 | 경로 |
|------|------|
| 프로젝트 개요 | `docs/01-project-overview.md` |
| 요구사항 정의 | `docs/02-requirements.md` |
| 도메인 모델 | `docs/03-domain.md` |
| ERD | `docs/04-erd.md` |
| API 명세 | `docs/05-api-spec.md` |
| 시스템 아키텍처 | `docs/06-architecture.md` |
| 개발 로드맵 | `docs/07-development-roadmap.md` |

---

## Author

**Junbamm** · [GitHub](https://github.com/Junbamm)
