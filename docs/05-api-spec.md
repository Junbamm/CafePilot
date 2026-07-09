# 05. API 명세 (API Specification)

본 문서는 [02-requirements.md](./02-requirements.md)의 기능 요구사항과 [04-erd.md](./04-erd.md)의 테이블 설계를 바탕으로 RESTful API 계약을 정의한다.

## 1. 공통 규칙

### 1.1 Base URL

```
/api/v1
```

### 1.2 인증

- 인증이 필요한 API는 요청 헤더에 JWT Access Token을 포함한다.
- `Authorization: Bearer {accessToken}`
- 토큰이 없거나 만료된 경우 `401 Unauthorized`를 반환한다.

### 1.3 공통 응답 포맷

모든 API 응답은 아래 구조를 따른다.

**성공**
```json
{
  "success": true,
  "data": { },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**실패**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "MENU_NOT_FOUND",
    "message": "해당 메뉴를 찾을 수 없습니다."
  },
  "timestamp": "2024-01-15T10:00:00"
}
```

### 1.4 공통 에러 코드

| 코드 | HTTP Status | 설명 |
|---|---|---|
| `COMMON_INVALID_INPUT` | 400 | 요청 값 검증 실패 |
| `COMMON_UNAUTHORIZED` | 401 | 인증 토큰 없음 또는 만료 |
| `COMMON_FORBIDDEN` | 403 | 권한 없음 |
| `COMMON_NOT_FOUND` | 404 | 리소스 없음 |
| `COMMON_INTERNAL_ERROR` | 500 | 서버 내부 오류 |
| `AUTH_INVALID_CREDENTIALS` | 401 | 이메일 또는 비밀번호 불일치 |
| `AUTH_EMAIL_DUPLICATED` | 409 | 이미 사용 중인 이메일 |
| `AUTH_TOKEN_EXPIRED` | 401 | Access Token 만료 |
| `CAFE_NOT_FOUND` | 404 | 카페 없음 |
| `CAFE_ACCESS_DENIED` | 403 | 본인 카페가 아님 |
| `MENU_NOT_FOUND` | 404 | 메뉴 없음 |
| `MENU_NOT_AVAILABLE` | 400 | 품절 또는 삭제된 메뉴 |
| `ORDER_NOT_FOUND` | 404 | 주문 없음 |
| `ORDER_INVALID_STATUS` | 400 | 허용되지 않는 상태 전이 |
| `INV_INSUFFICIENT` | 400 | 재고 부족 |
| `INV_NOT_FOUND` | 404 | 재고 없음 |

### 1.5 권한 정책 요약

| 역할 | 설명 |
|---|---|
| `OWNER` | 카페 소유자. 자신이 등록한 카페와 그 하위 리소스에 대한 전체 권한 |
| `STAFF` | 직원. 소속 카페의 주문 처리, 재고 확인 등 운영 권한 |
| `ADMIN` | 시스템 관리자. 전체 사용자, 운영 로그, 통계 접근 |

> 카페 소속 리소스(`/cafes/{cafeId}/...`)는 서버에서 cafeId 기반 소속 검증을 수행한다.
> 본인 카페가 아닌 리소스에 접근하면 `403 CAFE_ACCESS_DENIED`를 반환한다.

---

## 2. 인증 (Auth)

### 2.1 회원가입

```
POST /api/v1/auth/register
```

**권한**: 없음 (Public)

**Request Body**
```json
{
  "email": "owner@example.com",
  "password": "Password1!",
  "name": "홍길동",
  "role": "OWNER"
}
```

| 필드 | 타입 | 필수 | 검증 |
|---|---|---|---|
| email | String | Y | 이메일 형식 |
| password | String | Y | 최소 8자, 영문/숫자/특수문자 포함 |
| name | String | Y | 최소 1자, 최대 50자 |
| role | Enum | Y | `OWNER` 또는 `STAFF` |

**Response** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "owner@example.com",
    "name": "홍길동",
    "role": "OWNER"
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-AUTH-01, FR-AUTH-02, FR-AUTH-03, FR-AUTH-07

---

### 2.2 로그인

```
POST /api/v1/auth/login
```

**권한**: 없음 (Public)

**Request Body**
```json
{
  "email": "owner@example.com",
  "password": "Password1!"
}
```

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-AUTH-04, FR-AUTH-05

---

### 2.3 토큰 재발급

```
POST /api/v1/auth/refresh
```

**권한**: 없음 (Refresh Token으로 인증)

**Request Body**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-AUTH-06

---

### 2.4 내 정보 조회

```
GET /api/v1/members/me
```

**권한**: 인증된 모든 사용자

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "owner@example.com",
    "name": "홍길동",
    "role": "OWNER",
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:00:00"
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

---

## 3. 카페 (Cafe)

### 3.1 카페 등록

```
POST /api/v1/cafes
```

**권한**: `OWNER`

**Request Body**
```json
{
  "name": "홍길동 카페",
  "address": "서울시 강남구 테헤란로 123",
  "phone": "02-1234-5678",
  "businessHours": "09:00-21:00"
}
```

| 필드 | 타입 | 필수 | 검증 |
|---|---|---|---|
| name | String | Y | 최대 100자 |
| address | String | Y | 최대 255자 |
| phone | String | N | |
| businessHours | String | N | |

**Response** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동 카페",
    "address": "서울시 강남구 테헤란로 123",
    "phone": "02-1234-5678",
    "businessHours": "09:00-21:00",
    "createdAt": "2024-01-15T10:00:00"
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-CAFE-01

---

### 3.2 내 카페 목록 조회

```
GET /api/v1/cafes
```

**권한**: `OWNER` (본인 소유 카페만 반환), `STAFF` (소속 카페만 반환)

**Response** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "홍길동 카페",
      "address": "서울시 강남구 테헤란로 123",
      "phone": "02-1234-5678",
      "businessHours": "09:00-21:00",
      "createdAt": "2024-01-15T10:00:00"
    }
  ],
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-CAFE-02, FR-CAFE-04, FR-CAFE-05

---

### 3.3 카페 단건 조회

```
GET /api/v1/cafes/{cafeId}
```

**권한**: `OWNER` (본인 카페), `STAFF` (소속 카페)

**Response** `200 OK` — 3.1 응답과 동일

**관련 FR**: FR-CAFE-02

---

### 3.4 카페 정보 수정

```
PATCH /api/v1/cafes/{cafeId}
```

**권한**: `OWNER` (본인 카페만)

**Request Body** — 수정할 필드만 포함
```json
{
  "name": "홍길동 카페 강남점",
  "phone": "02-9999-9999"
}
```

**Response** `200 OK` — 수정된 카페 전체 정보 반환

**관련 FR**: FR-CAFE-03

---

## 4. 메뉴 (Menu)

### 4.1 메뉴 등록

```
POST /api/v1/cafes/{cafeId}/menus
```

**권한**: `OWNER`, `STAFF`

**Request Body**
```json
{
  "name": "아메리카노",
  "price": 4500,
  "description": "깊고 진한 에스프레소",
  "initialQuantity": 100,
  "thresholdQuantity": 10
}
```

| 필드 | 타입 | 필수 | 검증 |
|---|---|---|---|
| name | String | Y | 최대 100자 |
| price | Integer | Y | 1 이상 |
| description | String | N | |
| initialQuantity | Integer | Y | 0 이상 (메뉴 등록 시 초기 재고 함께 생성) |
| thresholdQuantity | Integer | Y | 0 이상 |

**Response** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 1,
    "cafeId": 1,
    "name": "아메리카노",
    "price": 4500,
    "description": "깊고 진한 에스프레소",
    "status": "ON_SALE",
    "inventory": {
      "quantity": 100,
      "thresholdQuantity": 10
    },
    "createdAt": "2024-01-15T10:00:00"
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

> 메뉴 등록 시 `inventories` 레코드도 함께 생성된다 (Menu-Inventory 1:1).

**관련 FR**: FR-MENU-01

---

### 4.2 메뉴 목록 조회

```
GET /api/v1/cafes/{cafeId}/menus
```

**권한**: `OWNER`, `STAFF`

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| status | Enum | (전체) | `ON_SALE`, `SOLD_OUT` 필터 |

**Response** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "아메리카노",
      "price": 4500,
      "status": "ON_SALE",
      "inventory": {
        "quantity": 100,
        "thresholdQuantity": 10
      }
    }
  ],
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-MENU-04, FR-MENU-05

---

### 4.3 메뉴 수정

```
PATCH /api/v1/cafes/{cafeId}/menus/{menuId}
```

**권한**: `OWNER`, `STAFF`

**Request Body** — 수정할 필드만 포함
```json
{
  "name": "아메리카노(ICE)",
  "price": 5000,
  "description": "시원한 아이스 아메리카노"
}
```

**Response** `200 OK` — 수정된 메뉴 전체 정보 반환

**관련 FR**: FR-MENU-02

---

### 4.4 메뉴 삭제

```
DELETE /api/v1/cafes/{cafeId}/menus/{menuId}
```

**권한**: `OWNER`, `STAFF`

> 물리 삭제가 아닌 `status = 'DELETED'` 처리

**Response** `200 OK`
```json
{
  "success": true,
  "data": null,
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-MENU-03

---

## 5. 주문 (Order)

### 5.1 주문 생성

```
POST /api/v1/cafes/{cafeId}/orders
```

**권한**: `OWNER`, `STAFF`

**Request Body**
```json
{
  "items": [
    { "menuId": 1, "quantity": 2 },
    { "menuId": 3, "quantity": 1 }
  ]
}
```

| 필드 | 타입 | 필수 | 검증 |
|---|---|---|---|
| items | Array | Y | 최소 1개 |
| items[].menuId | Long | Y | |
| items[].quantity | Integer | Y | 1 이상 |

**처리 흐름**
1. 각 menuId가 해당 카페 소속 메뉴인지 확인
2. 각 메뉴의 재고 충분 여부 확인 (부족 시 `INV_INSUFFICIENT`)
3. 주문 생성 (상태: `RECEIVED`)
4. 재고 차감 및 `InventoryTransaction` 기록
5. 주문 완료 이벤트 발행 (RabbitMQ)

**Response** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 101,
    "cafeId": 1,
    "status": "RECEIVED",
    "totalAmount": 14000,
    "items": [
      {
        "menuId": 1,
        "menuName": "아메리카노",
        "price": 4500,
        "quantity": 2,
        "subtotal": 9000
      },
      {
        "menuId": 3,
        "menuName": "카페라떼",
        "price": 5000,
        "quantity": 1,
        "subtotal": 5000
      }
    ],
    "createdAt": "2024-01-15T10:00:00"
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-ORDER-01, FR-ORDER-02, FR-ORDER-03, FR-ORDER-04

---

### 5.2 주문 목록 조회

```
GET /api/v1/cafes/{cafeId}/orders
```

**권한**: `OWNER`, `STAFF`

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| status | Enum | (전체) | `RECEIVED`, `IN_PROGRESS`, `COMPLETED`, `CANCELED` |
| date | Date | (오늘) | 조회 날짜 (yyyy-MM-dd) |
| page | Integer | 0 | 페이지 번호 (0-based) |
| size | Integer | 20 | 페이지 크기 |

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 101,
        "status": "RECEIVED",
        "totalAmount": 14000,
        "itemCount": 2,
        "createdAt": "2024-01-15T10:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-ORDER-06

---

### 5.3 주문 단건 조회

```
GET /api/v1/cafes/{cafeId}/orders/{orderId}
```

**권한**: `OWNER`, `STAFF`

**Response** `200 OK` — 5.1 응답의 `data`와 동일

**관련 FR**: FR-ORDER-06

---

### 5.4 주문 상태 변경

```
PATCH /api/v1/cafes/{cafeId}/orders/{orderId}/status
```

**권한**: `OWNER`, `STAFF`

**Request Body**
```json
{
  "status": "IN_PROGRESS"
}
```

**허용 상태 전이**

| 현재 상태 | 변경 가능 상태 |
|---|---|
| `RECEIVED` | `IN_PROGRESS`, `CANCELED` |
| `IN_PROGRESS` | `COMPLETED`, `CANCELED` |
| `COMPLETED` | - (변경 불가) |
| `CANCELED` | - (변경 불가) |

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 101,
    "status": "IN_PROGRESS",
    "updatedAt": "2024-01-15T10:05:00"
  },
  "error": null,
  "timestamp": "2024-01-15T10:05:00"
}
```

**관련 FR**: FR-ORDER-05, FR-ORDER-07, FR-ORDER-08

---

## 6. 재고 (Inventory)

### 6.1 재고 목록 조회

```
GET /api/v1/cafes/{cafeId}/inventories
```

**권한**: `OWNER`, `STAFF`

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| lowStock | Boolean | false | true 시 threshold 이하 품목만 반환 |

**Response** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "inventoryId": 1,
      "menuId": 1,
      "menuName": "아메리카노",
      "quantity": 8,
      "thresholdQuantity": 10,
      "isLowStock": true,
      "updatedAt": "2024-01-15T09:00:00"
    }
  ],
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-INV-04

---

### 6.2 재고 보충 (수동 입고)

```
PATCH /api/v1/cafes/{cafeId}/inventories/{inventoryId}/restock
```

**권한**: `OWNER`, `STAFF`

**Request Body**
```json
{
  "quantity": 50
}
```

| 필드 | 타입 | 필수 | 검증 |
|---|---|---|---|
| quantity | Integer | Y | 1 이상 |

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "inventoryId": 1,
    "menuName": "아메리카노",
    "previousQuantity": 8,
    "addedQuantity": 50,
    "currentQuantity": 58,
    "updatedAt": "2024-01-15T10:00:00"
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-INV-05

---

## 7. 매출 통계 (Sales)

### 7.1 일별 매출 조회

```
GET /api/v1/cafes/{cafeId}/sales/daily
```

**권한**: `OWNER`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| date | Date | N | 조회 날짜 (yyyy-MM-dd), 기본값: 오늘 |

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "cafeId": 1,
    "date": "2024-01-15",
    "totalSales": 450000,
    "totalOrderCount": 38,
    "averageOrderAmount": 11842
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-SALES-01, FR-SALES-04

---

### 7.2 월별 매출 조회

```
GET /api/v1/cafes/{cafeId}/sales/monthly
```

**권한**: `OWNER`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| year | Integer | Y | 연도 (예: 2024) |
| month | Integer | Y | 월 (1-12) |

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "cafeId": 1,
    "year": 2024,
    "month": 1,
    "totalSales": 12500000,
    "totalOrderCount": 1050,
    "dailySummaries": [
      { "date": "2024-01-01", "totalSales": 380000, "totalOrderCount": 32 },
      { "date": "2024-01-02", "totalSales": 420000, "totalOrderCount": 35 }
    ]
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-SALES-02, FR-SALES-04, FR-SALES-05

---

### 7.3 인기 메뉴 조회

```
GET /api/v1/cafes/{cafeId}/sales/top-menus
```

**권한**: `OWNER`

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| startDate | Date | Y | 시작 날짜 (yyyy-MM-dd) |
| endDate | Date | Y | 종료 날짜 (yyyy-MM-dd) |
| limit | Integer | N | 반환 개수 (기본값: 10) |

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "cafeId": 1,
    "period": { "startDate": "2024-01-01", "endDate": "2024-01-15" },
    "topMenus": [
      { "rank": 1, "menuId": 1, "menuName": "아메리카노", "totalQuantity": 450, "totalSales": 2025000 },
      { "rank": 2, "menuId": 3, "menuName": "카페라떼", "totalQuantity": 320, "totalSales": 1600000 }
    ]
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-SALES-03

---

## 8. AI 질의응답

### 8.1 AI 운영 데이터 질의

```
POST /api/v1/cafes/{cafeId}/ai/query
```

**권한**: `OWNER`

**Request Body**
```json
{
  "question": "이번 달 가장 많이 팔린 메뉴와 재고가 부족한 품목을 알려줘"
}
```

| 필드 | 타입 | 필수 | 검증 |
|---|---|---|---|
| question | String | Y | 최소 1자, 최대 500자 |

**처리 흐름**
1. 질문의 의도 분류 (매출/재고/인기메뉴/발주추천/추세분석)
2. 해당 카페의 운영 데이터 조회 (DB)
3. 조회 데이터를 컨텍스트로 LLM에 질의
4. 응답 반환

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "question": "이번 달 가장 많이 팔린 메뉴와 재고가 부족한 품목을 알려줘",
    "answer": "이번 달(2024년 1월 1일~15일) 가장 많이 팔린 메뉴는 아메리카노(450잔)입니다. 현재 재고가 부족한 품목은 카페라떼(잔여 8개, 기준 10개)입니다. 카페라떼 재고 보충을 권장합니다.",
    "dataContext": {
      "period": "2024-01-01 ~ 2024-01-15",
      "basedOn": ["sales_summary", "inventory"]
    }
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-AI-01, FR-AI-02, FR-AI-03, FR-AI-04, FR-AI-05, FR-AI-09

---

## 9. 관리자 (Admin)

> 모든 관리자 API는 `ROLE_ADMIN`만 접근 가능하다.

### 9.1 전체 회원 조회

```
GET /api/v1/admin/members
```

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| role | Enum | (전체) | `OWNER`, `STAFF`, `ADMIN` |
| status | Enum | (전체) | `ACTIVE`, `INACTIVE` |
| page | Integer | 0 | |
| size | Integer | 20 | |

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "email": "owner@example.com",
        "name": "홍길동",
        "role": "OWNER",
        "status": "ACTIVE",
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-ADMIN-01

---

### 9.2 회원 상태 변경 (활성/비활성)

```
PATCH /api/v1/admin/members/{memberId}/status
```

**Request Body**
```json
{
  "status": "INACTIVE"
}
```

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "INACTIVE",
    "updatedAt": "2024-01-15T10:00:00"
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-ADMIN-02

---

### 9.3 운영 로그 조회

```
GET /api/v1/admin/operation-logs
```

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|---|---|---|---|
| actorId | Long | (전체) | 특정 행위자 필터 |
| action | String | (전체) | 액션 필터 (예: ORDER_STATUS_CHANGE) |
| startDate | Date | (7일 전) | |
| endDate | Date | (오늘) | |
| page | Integer | 0 | |
| size | Integer | 20 | |

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "actorId": 1,
        "actorName": "홍길동",
        "action": "ORDER_STATUS_CHANGE",
        "targetType": "ORDER",
        "targetId": 101,
        "createdAt": "2024-01-15T10:05:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 500,
    "totalPages": 25
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-ADMIN-03

---

### 9.4 플랫폼 통계 조회

```
GET /api/v1/admin/stats
```

**Response** `200 OK`
```json
{
  "success": true,
  "data": {
    "totalMembers": 150,
    "totalCafes": 80,
    "totalOrdersToday": 1240,
    "totalSalesToday": 15600000
  },
  "error": null,
  "timestamp": "2024-01-15T10:00:00"
}
```

**관련 FR**: FR-ADMIN-04

---

## 10. API 목록 요약

| 메서드 | URL | 설명 | 권한 |
|---|---|---|---|
| POST | /api/v1/auth/register | 회원가입 | Public |
| POST | /api/v1/auth/login | 로그인 | Public |
| POST | /api/v1/auth/refresh | 토큰 재발급 | Public |
| GET | /api/v1/members/me | 내 정보 조회 | 인증 |
| POST | /api/v1/cafes | 카페 등록 | OWNER |
| GET | /api/v1/cafes | 내 카페 목록 | OWNER, STAFF |
| GET | /api/v1/cafes/{cafeId} | 카페 단건 조회 | OWNER, STAFF |
| PATCH | /api/v1/cafes/{cafeId} | 카페 수정 | OWNER |
| POST | /api/v1/cafes/{cafeId}/menus | 메뉴 등록 | OWNER, STAFF |
| GET | /api/v1/cafes/{cafeId}/menus | 메뉴 목록 조회 | OWNER, STAFF |
| PATCH | /api/v1/cafes/{cafeId}/menus/{menuId} | 메뉴 수정 | OWNER, STAFF |
| DELETE | /api/v1/cafes/{cafeId}/menus/{menuId} | 메뉴 삭제 | OWNER, STAFF |
| POST | /api/v1/cafes/{cafeId}/orders | 주문 생성 | OWNER, STAFF |
| GET | /api/v1/cafes/{cafeId}/orders | 주문 목록 조회 | OWNER, STAFF |
| GET | /api/v1/cafes/{cafeId}/orders/{orderId} | 주문 단건 조회 | OWNER, STAFF |
| PATCH | /api/v1/cafes/{cafeId}/orders/{orderId}/status | 주문 상태 변경 | OWNER, STAFF |
| GET | /api/v1/cafes/{cafeId}/inventories | 재고 목록 조회 | OWNER, STAFF |
| PATCH | /api/v1/cafes/{cafeId}/inventories/{inventoryId}/restock | 재고 보충 | OWNER, STAFF |
| GET | /api/v1/cafes/{cafeId}/sales/daily | 일별 매출 조회 | OWNER |
| GET | /api/v1/cafes/{cafeId}/sales/monthly | 월별 매출 조회 | OWNER |
| GET | /api/v1/cafes/{cafeId}/sales/top-menus | 인기 메뉴 조회 | OWNER |
| POST | /api/v1/cafes/{cafeId}/ai/query | AI 질의 | OWNER |
| GET | /api/v1/admin/members | 전체 회원 조회 | ADMIN |
| PATCH | /api/v1/admin/members/{memberId}/status | 회원 상태 변경 | ADMIN |
| GET | /api/v1/admin/operation-logs | 운영 로그 조회 | ADMIN |
| GET | /api/v1/admin/stats | 플랫폼 통계 조회 | ADMIN |
