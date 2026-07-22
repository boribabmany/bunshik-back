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

| 영역 | 설명 | 담당 |
|---|---|---|
| `common` | Entity 등 공용 코드. 먼저 만든 사람이 공유, 겹치지 않게 상의 후 작업 | 공동 |
| `kiosk` | 고객이 메뉴 조회, 주문, 결제하는 API | - |
| `admin` | 관리자 로그인, 메뉴/옵션/주문 관리, 변경 이력 조회 API | - |

같은 이름의 테이블(Entity)을 다루더라도, **Entity는 `common`에 하나만 두고 `kiosk`/`admin`이 각자 가져다 씁니다.** Controller·DTO·Mapper·Service는 담당 영역별로 완전히 분리되어 있어 파일명이 겹치지 않습니다.

---

## DB 설계

MySQL 기준, 총 9개 테이블로 구성됩니다.

| 테이블 | 설명 |
|---|---|
| `menus` | 메뉴 기본 정보 |
| `options` | 옵션(토핑) 목록 |
| `menu_options` | 메뉴-옵션 다대다 연결 |
| `orders` | 주문 (주문번호, 주문유형, 상태, 총액) |
| `order_items` | 주문에 담긴 메뉴들 |
| `order_item_options` | 주문 항목에 붙은 옵션 |
| `payments` | 결제 시도/결과 기록 |
| `admin_user` | 관리자 계정 |
| `admin_history` | 관리자 작업 변경 이력 |

전체 `CREATE TABLE` / `INSERT` 스크립트는 `docs/bunshik_db_setup.sql`(또는 팀 공유 문서)을 참고하세요.

### Entity ↔ 테이블 매칭 (완료된 9종)

| Entity 클래스 | 대응 테이블 |
|---|---|
| `Menu` | `menus` |
| `Option` | `options` |
| `MenuOption` | `menu_options` |
| `Order` | `orders` |
| `OrderItem` | `order_items` |
| `OrderItemOption` | `order_item_options` |
| `Payment` | `payments` |
| `AdminUser` | `admin_user` |
| `AdminHistory` | `admin_history` |

Entity는 MyBatis 사용을 기준으로 순수 자바 클래스(Lombok `@Getter`/`@Setter`)로 작성되어 있습니다. JPA로 접근하는 기능이 추가될 경우 별도 논의 후 `@Entity` 등 JPA 어노테이션 적용 여부를 결정합니다.

컬럼명(`menu_name`)과 필드명(`menuName`)의 매핑은 아래 설정으로 자동 처리됩니다.

```properties
mybatis.configuration.map-underscore-to-camel-case=true
```

---

## 진행 상황

### 완료

- [x] Gradle + Spring Boot 4.0.7 + Java 25 프로젝트 초기 세팅
- [x] `common.entity` 9종 (Menu, Option, MenuOption, Order, OrderItem, OrderItemOption, Payment, AdminUser, AdminHistory)
- [x] `common.ApiResponse` 공통 응답 형식
- [x] 원격 MySQL(팀 공유 DB) 연결 설정

### 진행 예정 (kiosk)

| 기능 | Mapper | DTO | Service | Controller |
|---|---|---|---|---|
| 메뉴 조회 | `MenuMapper` | `MenuResponseDto` | `MenuService` | `GET /api/menus` |
| 옵션 조회 | `OptionMapper` | `OptionResponseDto` | `OptionService` | `GET /api/options` |
| 주문 생성/조회 | `OrderMapper` | `OrderCreateRequestDto`, `OrderResponseDto` | `OrderService` | `POST /api/orders`, `GET /api/orders/{orderId}` |
| 결제 시도 | `PaymentMapper` | `PaymentRequestDto`, `PaymentResponseDto` | `PaymentService` | `POST /api/orders/{orderId}/payments` |

### 진행 예정 (admin, 담당자 별도 진행)

| 기능 | Controller |
|---|---|
| 관리자 로그인 | `POST /api/admin/login` |
| 메뉴/옵션 관리 | `POST/PUT/DELETE /api/admin/menus`, `/api/admin/options` |
| 주문 관리 | `GET /api/admin/orders`, `PATCH .../status`, `.../cancel` |
| 변경 이력 조회 | `GET /api/admin/history` |

---

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