# Deha-project

Human Resource Management (HRM) System

## Tech Stack
- Backend: Spring Boot (JWT auth, pagination/search with Spring Data)
- Frontend: React + Vite
- UI: Ant Design (`antd`)

## Notes / Implemented Improvements (Frontend)
- `Users`:
  - Search + pagination switched to **server-side** (requests send `page`/`size`, and UI uses `totalElements/totalPages` from backend).
  - UI pagination controls added (prev/next + page size).
- Modal & Loading UX:
  - Improved open/close transitions between `Department detail` and `Manage positions`.
  - Added loading overlay spinner when saving/updating positions and while department data refreshes.
  - Balanced panels inside `Department detail` so **Positions** and **Users** scroll internally (flex layout inside the modal).
- UI consistency:
  - Replaced native `input/select/textarea` in key flows with `antd` components (`Input`, `Select`, `Input.Password`, `Checkbox`).

## Notes / Implemented Improvements (Auth)
- Refresh token retry:
  - Frontend axios interceptor refresh now triggers on both `401` and `403` responses, improving recovery when access token expires.

## Project Progress (Current)
- Backend (Users)
  - `GET /api/users` supports pagination (Spring `Pageable`)
  - `GET /api/users/search?keyword=...` supports keyword search + pagination
- Frontend (Users)
  - Users list uses **server-side** search + pagination (with debounce for search keyword)
  - Pagination controls: prev/next + page size
- Frontend (Auth)
  - Refresh token retry improved on `401` / `403` (axios interceptor)
- Frontend (Departments)
  - `Department detail` modal: fixed modal size, internal flex layout; **Positions**/**Users** cards grow/shrink with internal scroll
  - `Manage positions` modal: fixed size (720x560), internal scroll, open/close transition, loading overlay (`Spin`)
- UI consistency
  - Migrated key form controls to Ant Design (`Input`, `Select`, `Input.Password`, `Checkbox`, `Input.TextArea`)

## Known Limitation / TODO
- Department/Position/Status dropdown filters on the Users page currently apply to the **current server page only**.
  - Server-side search/pagination uses backend supports `keyword` + `Pageable` only.
