# HRM Backend Flow Notes (Detailed): Security, OTP, MapStruct, Audit

Tai lieu nay mo ta chi tiet luong va cach code duoc to chuc trong project `HumanResourceManagement` cho 5 nhom chuc nang:

1. Security authentication va authorization
2. Forgot password
3. Verify OTP + reset password
4. MapStruct mapper
5. Audit logging

Muc tieu cua tai lieu: doc xong co the trace duoc request theo duong di Controller -> Service -> Redis/DB -> Response, va biet vi tri debug khi loi.

---

## 0) Tong quan kien truc lien quan

### 0.1. Thanh phan backend lien quan
- Security filter + oauth2 resource server: `SecurityConfig`
- Auth business: `AuthController`, `AuthService`, `JwtUtil`
- OTP/token tam thoi: `EmailVerificationService`, `TokenStoreService`
- Role + scope policy: `@PreAuthorize`, `AccessScopeService`
- Mapping: `HrmMapperConfig`, cac `*Mapper`, cac `*MapperSupport`
- Audit write trail: `WebMvcAuditConfig`, `AuditLoggingInterceptor`, `AuditLogService`

### 0.2. Nguyen tac thiet ke dang ap dung
- Validation cap endpoint + cap service (double guard)
- Fail-safe cho audit (audit loi khong duoc lam hong business flow)
- Token nhay cam (refresh) luu Redis + cookie HttpOnly, khong tra qua body
- Scope check o service de bao ve du lieu theo office/department

---

## 1) Security: Authentication va Authorization

## 1.1. Authentication flow (email/password)

### Entry point
- `POST /api/auth/login`
- Code: `AuthController.login(...)`

### Request path
1. Controller nhan `LoginRequest` va goi `AuthService.login(request, response)`.
2. Service goi `AuthenticationManager.authenticate(...)`.
3. `AuthenticationManager` dung `CustomUserDetailService.loadUserByUsername(email)` de tai user DB.
4. Neu password dung va account active:
   - Tao JWT access token (`JwtUtil.generateAccessToken`) voi claim `roles`.
   - Tao refresh token (`JwtUtil.generateRefreshToken`) va luu Redis key `refresh_token:{token}`.
   - Set cookie refresh token (`setRefreshCookie`) voi:
     - `HttpOnly = true`
     - `SameSite = Lax`
     - `Path = /api/auth`
     - `Secure` theo config `app.cookie.secure`.
5. Tra `LoginResponse` chua access token + thong tin user co ban.

### Error handling chinh
- Thieu email/password -> `BadRequestException`
- Auth principal khong hop le -> `UnauthorizedException`
- User inactive -> `ForbiddenException`

## 1.2. Refresh flow

### Entry point
- `POST /api/auth/refresh`

### Request path
1. Controller lay cookie `refresh_token`.
2. `AuthService.refresh(...)` doc userId tu Redis (`JwtUtil.getUserIdFromRefreshToken`).
3. Neu token hop le va user active:
   - Xoa refresh token cu (`jwtUtil.deleteToken`) -> rotate token.
   - Tao access token moi + refresh token moi.
   - Set lai cookie.
4. Tra login response moi.

### Muc dich rotate refresh token
- Giam nguy co replay token cu neu bi lo.

## 1.3. OAuth2 exchange flow

### Entry point
- `POST /api/auth/oauth2/exchange?code=...`

### Request path
1. Code mot lan duoc tao va luu Redis boi `AuthService.createOAuth2ExchangeCode(...)`.
2. `exchangeOAuth2Code` doc key `oauth2_exchange:{code}`.
3. Hop le -> xoa code ngay sau khi dung.
4. Tao access + refresh token nhu login thuong.

### Y nghia
- Tach login OAuth2 browser flow ra khoi API token flow.

## 1.4. Authorization flow (3 lop)

### Lop A: HTTP security filter
- `SecurityConfig.filterChain()`
  - PermitAll cho endpoint auth public (`/api/auth/login`, `/refresh`, `/verify-otp`, ...)
  - `anyRequest().authenticated()` cho endpoint con lai.

### Lop B: Role check tai controller
- `@PreAuthorize(...)` tren method controller.
- Vi du:
  - `hasAnyRole('ADMIN','MANAGER_OFFICE')`
  - `isAuthenticated()`
  - `permitAll()`

### Lop C: Data scope check tai service
- `AccessScopeService` enforce pham vi office/department/user.
- Vi du:
  - `assertCanManageOffice(targetOfficeId)`
  - `assertCanManageDepartment(targetDepartmentId)`
  - `assertCanAccessUser(targetUser)`

=> Role dung nhung sai scope van bi chan o service.

## 1.5. JWT -> GrantedAuthority mapping

- `SecurityConfig.jwtAuthenticationConverter()` doc claim `roles`.
- Moi role duoc chuyen thanh `ROLE_*`.
- Vi du claim `MANAGER_OFFICE` -> authority `ROLE_MANAGER_OFFICE`.

Dieu nay giup `@PreAuthorize("hasRole('MANAGER_OFFICE')")` hoat dong dung.

---

## 2) Forgot Password (request OTP)

### File chinh
- `AuthController.forgotPassword(...)`
- `EmailVerificationService.sendForgotPasswordOtp(...)`
- `TokenStoreService.generateOtp(...)`

### Flow chi tiet
1. Client gui email toi `POST /api/auth/forgot-password`.
2. Service tim user theo email:
   - Khong ton tai hoac inactive -> ket thuc im lang.
   - Ton tai va active -> tao OTP 6 chu so.
3. OTP luu Redis key `otp:{email}`, TTL 5 phut.
4. Gui OTP qua email.
5. API tra thong diep trung tinh: "If the email exists, an OTP has been sent".

### Ly do thong diep trung tinh
- Chong user enumeration (khong de lo email nao ton tai).

---

## 3) Verify OTP va Reset Password

## 3.1. Verify OTP

### Entry point
- `POST /api/auth/verify-otp`

### Flow
1. `EmailVerificationService.verifyOtp(request)` goi `tokenStoreService.validateOtp(email, otp)`.
2. Sai/het han -> `BadRequestException`.
3. Dung -> xoa OTP cu (`deleteOtp`).
4. Tao `reset token` (UUID token luu Redis key `reset_token:*`, TTL 15 phut).
5. Tra `VerifyOtpResponse(resetToken)`.

### Security note
- OTP la one-time: dung xong bi xoa.

## 3.2. Reset password

### Entry point
- `POST /api/auth/reset-password`

### Flow
1. Service doc userId tu `reset token` trong Redis.
2. Neu token invalid/expired -> `BadRequestException`.
3. Tim user, ma hoa password moi bang `PasswordEncoder`.
4. Save DB, xoa reset token.

## 3.3. TTL matrix (Redis)
- Verify token: 24h (`verify_token:*`)
- OTP: 5 phut (`otp:*`)
- Reset token: 15 phut (`reset_token:*`)

---

## 4) MapStruct Mapper

## 4.1. Vi sao dung MapStruct
- Bo map thu cong lap di lap lai.
- Convert nhanh va an toan type (generate luc compile).
- De quy dinh mapping theo module + helper support class.

## 4.2. Cau hinh chung
- `HrmMapperConfig`:
  - `componentModel = "spring"` -> co the inject mapper trong service/controller.
  - `unmappedTargetPolicy = IGNORE` -> khong fail compile neu field chua map.

## 4.3. Mau map nested object
- `UserMapper` map:
  - `office` -> `officeId`, `officeName`
  - `department` -> `departmentId`, `departmentName`
  - `position` -> `positionId`, `positionName`
- Logic nested duoc dat trong `CoreOrgMapperSupport` bang methods co `@Named`.

### Vi du pattern
- Mapper interface chi khai bao contract.
- Support class chua helper methods tai su dung duoc cho nhieu mapper.

## 4.4. Build-time generation
- Maven compiler plugin co `mapstruct-processor`.
- Khi compile, implementation mapper duoc generate tu dong.

## 4.5. Quy tac mo rong mapper moi
1. Tao DTO response/request.
2. Tao mapper interface, `@Mapper(config = HrmMapperConfig.class, uses = ...Support.class)`.
3. Neu can mapping phuc tap, bo helper vao `*MapperSupport`.
4. Update service dung mapper thay vi `fromEntity` thu cong.

---

## 5) Audit Logging

## 5.1. Muc tieu
- Ghi nhan cac write action quan trong he thong (khong ghi read tran lan).
- Dam bao truy vet duoc actor, endpoint, ket qua, thoi gian xu ly.

## 5.2. Diem dat interceptor
- `WebMvcAuditConfig` gan `AuditLoggingInterceptor` vao `/api/**`.
- Exclude swagger/docs.

## 5.3. Interceptor flow

### preHandle
1. `shouldAuditRequest(request)` quyet dinh co audit hay khong.
2. Danh dau `audit.enabled` trong request attribute.
3. Neu co audit -> luu `audit.startNanos`.

### afterCompletion
1. Neu `audit.enabled != true` -> bo qua.
2. Tinh duration millisecond.
3. Goi `AuditLogService.logHttpWrite(request, statusCode, durationMs)`.

## 5.4. Scope audit hien tai
- Chi method write: `POST`, `PUT`, `PATCH`, `DELETE`.
- `DELETE /api/**` duoc audit rong de truy vet xoa du lieu.
- Cac module duoc allowlist ro rang:
  - auth/security
  - users/departments/positions/offices
  - salary-contracts/payrolls/ot-requests/ot-reports/ot-sessions

## 5.5. Persist audit record

`AuditLogService` tao record gom:
- HTTP: method, requestUri, endpointPattern, statusCode, success
- Identity: actorEmail, actorUserId, actorOfficeId
- Context: targetId (neu path co `{id}`), clientIp, userAgent, durationMs

Ky thuat dang dung:
- `@Transactional(REQUIRES_NEW)` de audit commit doc lap transaction business.
- try/catch fail-safe: neu save audit loi -> warn log, khong nem exception.

## 5.6. Query/export audit
- `AuditLogController`:
  - `GET /api/audit-logs` (paging + filter)
  - `GET /api/audit-logs/{id}`
  - `GET /api/audit-logs/export.csv`
- Role: `ADMIN`, `MANAGER_OFFICE`.

---

## 6) Checklist debug nhanh

## 6.1. Loi 401/403
1. Kiem tra endpoint co nam trong permitAll hay khong (`SecurityConfig`).
2. Kiem tra role claim trong JWT co map thanh `ROLE_*` hay khong.
3. Kiem tra `@PreAuthorize` tren method controller.
4. Neu da qua controller nhung van fail -> xem `AccessScopeService` (scope office/department).

## 6.2. Loi OTP/reset
1. Kiem tra Redis key co ton tai va con TTL.
2. OTP da bi delete sau verify chua (one-time).
3. Reset token dung user va chua het han.

## 6.3. Loi mapping
1. Kiem tra mapper interface da co `@Mapper(config = HrmMapperConfig.class)`.
2. Kiem tra helper method co `@Named` dung ten trong `qualifiedByName`.
3. Kiem tra build plugin co `mapstruct-processor`.

## 6.4. Loi audit khong ghi
1. Request co la write method khong.
2. Path co nam trong allowlist khong.
3. `AuditLoggingInterceptor` da duoc register trong `WebMvcAuditConfig` chua.
4. Xem warning log "Failed to persist audit log" de biet loi DB/constraint.

---

## 7) Summary ngan
- Authentication: username/password or oauth2 exchange -> access JWT + refresh cookie.
- Authorization: 3 lop bao ve (filter chain -> `@PreAuthorize` -> data scope service).
- Forgot/OTP/reset: token tam thoi luu Redis voi TTL ro rang, one-time consume.
- MapStruct: mapper theo module + helper support, generate luc compile.
- Audit: interceptor scoped + save doc lap transaction + fail-safe.

Tai lieu nay co the duoc mo rong tiep bang sequence diagram (Mermaid) cho login, refresh, forgot-password, verify-otp, audit pipeline.

