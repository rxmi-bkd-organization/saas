---
paths:
  - "frontend/src/**/*.ts"
---

# Frontend: `core/http` usage

- Classes in `frontend/src/app/core/http/` (`*.http.ts`) are low-level HTTP clients.
- They may only be injected/imported by services in `frontend/src/app/core/service/` (`*.service.ts`).
- Components, pages, guards, interceptors and anything else must go through a service in `core/service/`, never an `*.http.ts` class directly.
- When a component needs new backend data, add or extend a method in the matching `core/service/` service (and the `core/http/` class behind it) instead of importing the http class.
