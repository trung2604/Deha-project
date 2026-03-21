# Deha-project

Human Resource Management (HRM) System

## Tech Stack
- Backend: Spring Boot (JWT auth, pagination/search with Spring Data)
- Frontend: React + Vite
- UI: Ant Design (`antd`)

## Notes / Implemented Improvements (Frontend)
- `Users`:
  - Search + pagination switched to **server-side** (requests send `page`/`size`, and UI uses `totalElements/totalPages` from backend).
  - Added server-side filters for `department`, `position`, `status(active/inactive)` and synced filter state with pagination.
  - UI pagination controls added (prev/next + page size).
  - Refined `UserTable` layout to fit viewport better and reduced column overflow.
- Modal & Loading UX:
  - Improved open/close transitions between `Department detail` and `Manage positions`.
  - Added loading overlay spinner when saving/updating positions and while department data refreshes.
  - Balanced panels inside `Department detail` so **Positions** and **Users** scroll internally (flex layout inside the modal).
- UI consistency:
  - Replaced native `input/select/textarea` in key flows with `antd` components (`Input`, `Select`, `Input.Password`, `Checkbox`).

## Notes / Implemented Improvements (Auth)
- Refresh token retry:
  - Frontend axios interceptor refresh now triggers on both `401` and `403` responses, improving recovery when access token expires.
- Auth/controller-service refactor:
  - Moved auth business logic from `AuthController` to `AuthService` to keep controller thin.
- Profile update:
  - Added `PUT /api/auth/me` and frontend editable profile flow (first name, last name, phone with validation).
- JWT authority mapping fix:
  - Fixed role authority prefix handling in `JwtFilter` (`ROLE_` no longer duplicated), preventing false `403` after login.

## Project Progress (Current)
- Backend (Users)
  - `GET /api/users` supports pagination + optional filters: `keyword`, `departmentId`, `positionId`, `active`
  - `GET /api/users/search?keyword=...` supports keyword search + optional filters + pagination
  - User query refactored to native SQL for stable filtered pagination on PostgreSQL
- Frontend (Users)
  - Users list uses **server-side** search + pagination (with debounce for search keyword)
  - Department/Position/Status filters are now server-side (global on dataset, not page-local)
  - Pagination controls: prev/next + page size
- Frontend (Auth)
  - Refresh token retry improved on `401` / `403` (axios interceptor)
  - Auth context/API response handling standardized with shared response helpers
- Frontend (Departments)
  - `Department detail` modal: fixed modal size, internal flex layout; **Positions**/**Users** cards grow/shrink with internal scroll
  - `Manage positions` modal: improved fixed-size layout, stable list scroll behavior, open/close transition, loading overlay (`Spin`)
  - Fixed modal state flow so opening a department no longer auto-opens `Manage positions`
- UI consistency
  - Migrated key form controls to Ant Design (`Input`, `Select`, `Input.Password`, `Checkbox`, `Input.TextArea`)
  - Standardized API response parsing in FE with `src/utils/apiResponse.js` and applied across Users/Auth/Departments flows

## Known Limitation / TODO
- Dashboard/search/notification data in header are still placeholder UI (no real backend wiring yet).
- Password change flow in profile security tab still pending backend endpoint.
