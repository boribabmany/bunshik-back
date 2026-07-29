# bunshik-back

분식 주문 키오스크 백엔드 프로젝트

`kiosk-customer`(고객 키오스크), `kiosk-admin`(관리자 페이지) 두 프론트엔드가 함께 사용하는 백엔드 API 서버입니다.

---

## 목차

- [기술 스택](#기술-스택)
- [담당 구분](#담당-구분)
- [DB 설계](#db-설계)
- [진행 상황](#진행-상황)
- [실행 방법](#실행-방법)
- [주의사항](#주의사항)

---

## 기술 스택

- **Language**: Java 25
- **Framework**: Spring Boot 4.0.7
- **Build Tool**: Gradle
- **Database**: MySQL
- **ORM / 데이터 접근**: Spring Data JPA + MyBatis (병행 사용)
- **인증**: Spring Security + JWT
- **기타**: Lombok, Validation

> JPA와 MyBatis를 함께 쓰는 이유: 단순한 CRUD는 JPA로, 복잡한 조회(통계, 다중 조인 등)는 MyBatis로 직접 SQL을 작성해 처리할 수 있도록 병행 구성했습니다. 어떤 기능에 어떤 방식을 쓸지는 기능 구현 시점에 팀 내에서 정합니다.

---

## 담당 구분

| 영역     | 설명                                                                 | 담당 |
| -------- | -------------------------------------------------------------------- | ---- |
| `common` | Entity 등 공용 코드. 먼저 만든 사람이 공유, 겹치지 않게 상의 후 작업 | 공동 |
| `kiosk`  | 고객이 메뉴 조회, 주문, 결제하는 API                                 | -    |
| `admin`  | 관리자 로그인, 메뉴/옵션/주문 관리, 변경 이력 조회 API               | -    |

같은 이름의 테이블(Entity)을 다루더라도, **Entity는 `common`에 하나만 두고 `kiosk`/`admin`이 각자 가져다 씁니다.** Controller·DTO·Mapper·Service는 담당 영역별로 완전히 분리되어 있어 파일명이 겹치지 않습니다.

---

## DB 설계

MySQL 기준, 총 9개 테이블로 구성됩니다.

| 테이블               | 설명                                  |
| -------------------- | ------------------------------------- |
| `menus`              | 메뉴 기본 정보                        |
| `options`            | 옵션(토핑) 목록                       |
| `menu_options`       | 메뉴-옵션 다대다 연결                 |
| `orders`             | 주문 (주문번호, 주문유형, 상태, 총액) |
| `order_items`        | 주문에 담긴 메뉴들                    |
| `order_item_options` | 주문 항목에 붙은 옵션                 |
| `payments`           | 결제 시도/결과 기록                   |
| `admin_user`         | 관리자 계정                           |
| `admin_history`      | 관리자 작업 변경 이력                 |

전체 `CREATE TABLE` / `INSERT` 스크립트는 `docs/bunshik_db_setup.sql`(또는 팀 공유 문서)을 참고하세요.

### `menus` 테이블 컬럼 상세

| 컬럼                             | 타입     | 설명                                                                          |
| -------------------------------- | -------- | ----------------------------------------------------------------------------- |
| `menu_id`                        | INT (PK) | 메뉴 고유 ID                                                                  |
| `menu_name`                      | VARCHAR  | 메뉴명(한글)                                                                  |
| `menu_name_en`                   | VARCHAR  | 메뉴명(영문)                                                                  |
| `price`                          | INT      | 가격                                                                          |
| `category`                       | VARCHAR  | 카테고리                                                                      |
| `image_url`                      | VARCHAR  | 이미지 경로 (상대경로로 저장, 예: `/uploads/menus/xxx.webp`)                  |
| `description` / `description_en` | VARCHAR  | 메뉴 설명(한/영)                                                              |
| `is_available`                   | BOOLEAN  | **품절 여부**. `false`면 화면에는 노출되지만 "품절" 표시 + 선택 불가          |
| `is_visible`                     | BOOLEAN  | **판매중단 여부**. `false`면 화면에서 완전히 숨김 (조회 쿼리 자체에서 필터링) |
| `sort_order`                     | INT      | 메뉴 노출 순서. 값이 작을수록 먼저 표시됨 (`ORDER BY sort_order ASC`)         |
| `sold_out_reason`                | VARCHAR  | 품절 사유(선택)                                                               |
| `created_at` / `updated_at`      | DATETIME | 생성/수정 시각                                                                |

> `is_available`(품절)과 `is_visible`(판매중단)은 서로 다른 개념입니다. 품절은 일시적(재고 소진)이라 메뉴가 보이되 선택만 막고, 판매중단은 메뉴 자체를 화면에서 제거합니다.

이미지 경로는 DB에 항상 **상대경로**로 저장하며(`/uploads/...`), 프론트에서 API 응답을 받을 때 `getImageUrl()` 헬퍼로 백엔드 base URL을 붙여 완성된 절대경로로 변환해 사용합니다.

### Entity ↔ 테이블 매칭 (완료된 9종)

| Entity 클래스     | 대응 테이블          |
| ----------------- | -------------------- |
| `Menu`            | `menus`              |
| `Option`          | `options`            |
| `MenuOption`      | `menu_options`       |
| `Order`           | `orders`             |
| `OrderItem`       | `order_items`        |
| `OrderItemOption` | `order_item_options` |
| `Payment`         | `payments`           |
| `AdminUser`       | `admin_user`         |
| `AdminHistory`    | `admin_history`      |

Entity는 MyBatis 사용을 기준으로 순수 자바 클래스(Lombok `@Getter`/`@Setter`)로 작성되어 있습니다. JPA로 접근하는 기능이 추가될 경우 별도 논의 후 `@Entity` 등 JPA 어노테이션 적용 여부를 결정합니다.

컬럼명(`menu_name`)과 필드명(`menuName`)의 매핑은 아래 설정으로 자동 처리됩니다.

```properties
mybatis.configuration.map-underscore-to-camel-case=true
```

---

## 진행 상황

### 완료 (kiosk)

| 기능                  | Mapper          | DTO                                         | Service          | Controller                           |
| --------------------- | --------------- | ------------------------------------------- | ---------------- | ------------------------------------ |
| 메뉴 조회             | `MenuMapper`    | `MenuResponseDto`                           | (Menu 조회 로직) | `GET /api/menus`                     |
| 주문 생성             | `OrderMapper`   | `OrderCreateRequestDto`, `OrderResponseDto` | `OrderService`   | `POST /api/orders`                   |
| 주문 취소 (결제 포기) | `OrderMapper`   | -                                           | `OrderService`   | `PATCH /api/orders/{orderId}/cancel` |
| 결제 시도             | `PaymentMapper` | `PaymentRequestDto`, `PaymentResponseDto`   | `PaymentService` | `POST /api/payments`                 |

### 진행 예정 / 확인 필요 (kiosk)

| 기능           | Controller                  | 비고                                                                                           |
| -------------- | --------------------------- | ---------------------------------------------------------------------------------------------- |
| 옵션 단독 조회 | `GET /api/options`          | 현재 메뉴 조회 응답에 옵션이 중첩(nested)되어 함께 내려감. 별도 엔드포인트 필요 여부 확인 필요 |
| 주문 상세 조회 | `GET /api/orders/{orderId}` | 미구현. 새로고침 시 주문 내역 복구가 필요해지면 별도 구현 논의 필요                            |

- [x] Gradle + Spring Boot 4.0.7 + Java 25 프로젝트 초기 세팅
- [x] `common.entity` 9종 (Menu, Option, MenuOption, Order, OrderItem, OrderItemOption, Payment, AdminUser, AdminHistory)
- [x] `common.ApiResponse` 공통 응답 형식
- [x] 원격 MySQL(팀 공유 DB) 연결 설정

| 기능           | Mapper          | DTO                                         | Service          | Controller                                      |
| -------------- | --------------- | ------------------------------------------- | ---------------- | ----------------------------------------------- |
| 메뉴 조회      | `MenuMapper`    | `MenuResponseDto`                           | `MenuService`    | `GET /api/menus`                                |
| 옵션 조회      | `OptionMapper`  | `OptionResponseDto`                         | `OptionService`  | `GET /api/options`                              |
| 주문 생성/조회 | `OrderMapper`   | `OrderCreateRequestDto`, `OrderResponseDto` | `OrderService`   | `POST /api/orders`, `GET /api/orders/{orderId}` |
| 결제 시도      | `PaymentMapper` | `PaymentRequestDto`, `PaymentResponseDto`   | `PaymentService` | `POST /api/orders/{orderId}/payments`           |

---

## 주문 상태(order_status) 관리 규칙

`orders.order_status`는 다음 값을 가지며, 정해진 순서로만 전이됩니다.

### 완료 (admin)

| 기능              | 주요 구성                                                                              | Controller / API                   |
| ----------------- | -------------------------------------------------------------------------------------- | ---------------------------------- |
| 관리자 로그인·JWT | `AdminAuthMapper`, `AdminLoginRequestDto`, `AdminLoginResponseDto`, `AdminAuthService` | `POST /api/admin/login`            |
| 메뉴 관리         | `AdminMenuMapper`, `AdminMenuRequestDto`, `AdminMenuService`                           | 조회·등록·수정·판매중단·판매재개   |
| 옵션 관리         | `AdminOptionMapper`, `AdminOptionRequestDto`, `AdminOptionService`                     | 조회·등록·수정·판매중단·판매재개   |
| 주문 관리         | `AdminOrderMapper`, 검색/상태 DTO, `AdminOrderService`                                 | 목록·상세·검색·상태 변경·취소      |
| 변경 이력         | `AdminHistoryMapper`, `AdminHistoryResponseDto`, `AdminHistoryService`                 | `GET /api/admin/history`           |
| 매출 대시보드     | `AdminSalesMapper`, 매출 응답 DTO 3종, `AdminSalesService`                             | 요약·인기 메뉴·최근 30일 매출      |
| 이미지 제공       | `AdminFileController`                                                                  | `GET /uploads/{folder}/{filename}` |

#### 관리자 API

##### 인증·파일·이력

| Method | Endpoint                       | 설명                                |
| ------ | ------------------------------ | ----------------------------------- |
| POST   | `/api/admin/login`             | 관리자 인증 및 JWT 액세스 토큰 발급 |
| GET    | `/uploads/{folder}/{filename}` | 업로드된 메뉴·옵션 이미지 조회      |
| GET    | `/api/admin/history`           | 관리자 작업 변경 이력 조회          |

##### 메뉴

| Method | Endpoint                           | 설명                                  |
| ------ | ---------------------------------- | ------------------------------------- |
| GET    | `/api/admin/menus`                 | 판매중단 항목을 포함한 전체 메뉴 조회 |
| GET    | `/api/admin/menus/{menuId}`        | 메뉴 상세 조회                        |
| POST   | `/api/admin/menus`                 | 메뉴 및 이미지 등록                   |
| PUT    | `/api/admin/menus/{menuId}`        | 메뉴 및 이미지 수정                   |
| PATCH  | `/api/admin/menus/{menuId}/stop`   | 메뉴 판매중단 (`is_visible=false`)    |
| PATCH  | `/api/admin/menus/{menuId}/resume` | 메뉴 판매재개                         |

##### 옵션

| Method | Endpoint                               | 설명                                  |
| ------ | -------------------------------------- | ------------------------------------- |
| GET    | `/api/admin/options`                   | 판매중단 항목을 포함한 전체 옵션 조회 |
| GET    | `/api/admin/options/{optionId}`        | 옵션 상세 조회                        |
| POST   | `/api/admin/options`                   | 옵션 및 이미지 등록                   |
| PUT    | `/api/admin/options/{optionId}`        | 옵션 및 이미지 수정                   |
| PATCH  | `/api/admin/options/{optionId}/stop`   | 옵션 판매중단 (`is_visible=false`)    |
| PATCH  | `/api/admin/options/{optionId}/resume` | 옵션 판매재개                         |

##### 주문

| Method | Endpoint                             | 설명                                         |
| ------ | ------------------------------------ | -------------------------------------------- |
| GET    | `/api/admin/orders`                  | `결제대기`, `취소`를 제외한 주문 목록 조회   |
| GET    | `/api/admin/orders/{orderId}`        | 주문 기본 정보 조회                          |
| GET    | `/api/admin/orders/search`           | `date`, `orderType`, `orderStatus` 조건 검색 |
| PATCH  | `/api/admin/orders/{orderId}/status` | 요청한 상태로 주문 상태 변경                 |
| PATCH  | `/api/admin/orders/{orderId}/cancel` | 주문 상태를 `취소`로 변경                    |

##### 매출

| Method | Endpoint                   | 설명                                                     |
| ------ | -------------------------- | -------------------------------------------------------- |
| GET    | `/api/admin/sales/summary` | 오늘·이번 달·어제 매출, 평균 주문금액, 완료 주문 수 조회 |
| GET    | `/api/admin/sales/popular` | 취소 주문을 제외한 인기 메뉴 TOP 5 조회                  |
| GET    | `/api/admin/sales/history` | 취소 주문을 제외한 최근 30일 일별 매출·주문 수 조회      |

메뉴 등록은 각 DTO 필드와 필수 `file`을 `multipart/form-data`로 전송합니다. 메뉴 수정 시 `file`은 선택 사항입니다. 옵션 등록·수정은 `option`이라는 JSON 문자열 파트와 선택적 `file` 파트를 사용합니다.

#### 관리자 인증 규칙

- `POST /api/admin/login`은 인증 없이 접근할 수 있습니다.
- `/api/admin/**`는 `ROLE_ADMIN` 권한이 있는 JWT가 필요합니다.
- 클라이언트는 `Authorization: Bearer <accessToken>` 헤더를 전송합니다.
- 서버는 세션을 만들지 않는 `STATELESS` 방식으로 동작합니다.
- 인증 실패와 권한 부족은 각각 JSON 형식의 `401`, `403` 응답으로 처리합니다.

---

### kiosk(고객) 측 규칙

담당 파일: `OrderService.java`, `PaymentService.java`

- 주문 생성(`POST /api/orders`) 시 `order_status`는 항상 `결제대기`로 시작합니다.
- 결제 성공(`POST /api/payments`) 시 → `접수`로 전환됩니다.
- **결제 실패 시 → `취소`가 아닌 `결제대기`를 유지**합니다. 카드 거절, 응답 지연 등은 일시적 실패이므로, 손님이 재시도(다른 카드, 다시 결제)할 수 있도록 주문을 살려둡니다.
- 손님이 결제를 포기하고 뒤로가기를 누르면 → `PATCH /api/orders/{orderId}/cancel`을 호출합니다. 단, **`결제대기` 상태인 주문만 취소 가능**하며, 이미 `접수`/`조리중`/`완료`로 넘어간 주문은 `IllegalStateException`으로 차단됩니다.
- **중복 결제 차단**: 동일 `order_id`에 대해 이미 `payment_status='성공'` 기록이 존재하면, 재결제 요청은 `IllegalArgumentException`("이미 결제가 완료된 주문입니다.")으로 거부됩니다. (`PaymentService.processPayment`)

| 상태 전이           | 트리거                           | 처리 위치                       |
| ------------------- | -------------------------------- | ------------------------------- |
| `결제대기` 생성     | 주문 생성                        | `OrderService.createOrder`      |
| `결제대기` → `접수` | 결제 성공                        | `PaymentService.processPayment` |
| `결제대기` 유지     | 결제 실패                        | `PaymentService.processPayment` |
| `결제대기` → `취소` | 손님이 결제 포기                 | `OrderService.cancelOrder`      |
| (차단)              | `결제대기`가 아닌 주문 취소 시도 | `OrderService.cancelOrder`      |
| (차단)              | 이미 성공한 주문 재결제 시도     | `PaymentService.processPayment` |

### admin(관리자) 측 규칙

담당 파일: `AdminOrderService.java`, `OrderMapper.xml`

- 주문 목록은 현재 쿼리 기준 `결제대기`, `취소` 상태를 제외하고 최신 주문부터 반환합니다.
- 주문 검색은 날짜(`date`), 주문 유형(`orderType`), 주문 상태(`orderStatus`)를 선택적으로 조합합니다.
- 주문 상태 변경은 요청 본문의 `orderStatus` 값을 저장하고 관리자 변경 이력을 남깁니다.
- 주문 취소는 레코드를 삭제하지 않고 `order_status='취소'`로 변경한 뒤 변경 이력을 남깁니다.
- 현재 서비스에는 `접수 → 조리중 → 완료` 순서 강제나 완료 주문 취소 차단 검증이 구현되어 있지 않습니다. 해당 정책이 필요하면 상태 변경·취소 전에 서버 검증을 추가해야 합니다.
- `GET /api/admin/orders/{orderId}`는 현재 주문 기본 정보만 반환하며 주문 항목과 선택 옵션은 포함하지 않습니다.

관리자 API의 상세 요청·응답 예시는 DevProject Hub 5번 워크스페이스 API 명세서를 기준으로 관리합니다.

## 실행 방법

```bash
git clone https://github.com/boribabmany/bunshik-back.git
cd bunshik-back
```

`application.properties`에 DB 접속 정보 채운 후:

```bash
./gradlew bootRun
```

정상 실행 시 콘솔에 아래와 같은 메시지가 출력됩니다.

```
HikariPool-1 - Start completed.
Started BunshikBackApplication in X.XXX seconds
```

---

## 주의사항

- `application.properties`, `.env` 등 민감 정보가 담긴 파일은 절대 커밋하지 않습니다. `.gitignore`에 등록되어 있는지 항상 확인합니다.
- `kiosk`, `admin` 각자 담당 패키지만 수정하고, `common`을 수정할 때는 상대방과 미리 상의합니다.
- 커밋 전 `git status`로 올라갈 파일을 확인하는 습관을 유지합니다.
- 프론트엔드(`kiosk-customer`, `kiosk-admin`)의 API 호출 함수(`menuApi.js` 등)는 백엔드 엔드포인트가 준비되는 대로 목업 코드를 실제 `fetch` 호출로 교체합니다.
