# VeriCode Performance Hardening Checklist

This checklist is the project's baseline performance review and should be reused for future projects.

| # | Check | Status | Implementation / note |
|---|---|---|---|
| 1 | Compress all images | N/A | The current repository contains no image assets to compress. |
| 2 | Add lazy loading | Done | `QuestionImport` is loaded with a dynamic `import()` so the feature is deferred until needed. |
| 3 | Split code into chunks | Done | Vite/Rolldown code splitting is enabled and vendor chunks are separated. |
| 4 | Cache API responses | Done | Server-side Caffeine caching covers question reads and question-import results. User-specific assessment/submission responses are not cached. |
| 5 | Add a CDN | Ready | `VITE_CDN_URL` can provide the Vite asset base URL. A real CDN must be provisioned in deployment; no provider URL is invented in source. |
| 6 | Minify JS and CSS | Done | Vite production builds explicitly use Oxc JS minification and Lightning CSS minification. |
| 7 | Index the database | Done | Indexes added for question lookup, assessment recruiter lookup, submission filters, test-case question lookup, and candidate email lookup. |
| 8 | Reduce unnecessary re-renders | Partially audited | Existing `useMemo` is retained for question filtering; the codebase does not justify aggressive memoization of every component. |
| 9 | Debounce input handlers | Not needed | Search is local in-memory filtering and code-editor changes are tracked as behavior events, so debouncing those handlers would add latency or lose fidelity. |
| 10 | Paginate large lists | Done at API level | Added `GET /Questions/page` with a default page size of 20 and a maximum of 100. |
| 11 | Remove unused dependencies | Audited | React/ReactDOM are runtime dependencies; the listed Vite/ESLint packages are used by the project configuration. No clearly unused dependency was removed blindly. |
| 12 | Defer non-critical work | Done | Question import UI is dynamically loaded instead of being part of the initial bundle. |
| 13 | Add loading skeleton | Done | Lazy-loaded question import has a loading skeleton fallback. |
| 14 | Add a load balancer | Done as deployment config | `deployment/nginx.conf` defines two backend targets with session affinity. |
| 15 | Compress API payloads | Done | Spring HTTP response compression is enabled for common text/JSON MIME types. |
| 16 | Add database connection pooling | Done | HikariCP pool limits/timeouts are explicitly configured. |
| 17 | Cache expensive computed results | Done | Question import results are cached because they require external HTTP calls and parsing. |
| 18 | Fix N+1 database queries | Mitigated | Hibernate batch fetching is enabled with a batch size of 25 to reduce repeated lazy-association round trips. |
| 19 | Enable server-side caching | Done | Spring Cache + Caffeine are enabled with bounded size and expiration. |
| 20 | Run a Lighthouse audit | Pending deployment | The repository has no deployed frontend URL. A real Lighthouse score cannot be honestly reported until the frontend is deployed. |

## Deployment requirements before calling the project production-ready

1. Set `VITE_CDN_URL` to the actual CDN asset origin when deploying the frontend.
2. Deploy the frontend behind the Nginx configuration in `deployment/nginx.conf` or an equivalent edge/load-balancing layer.
3. Because the current authentication uses `HttpSession`, keep session affinity or move sessions to a shared store such as Redis before scaling backend instances without affinity.
4. Run the CI build and tests after deployment.
5. Run Lighthouse against the deployed HTTPS URL and record the actual Performance, Accessibility, Best Practices, and SEO scores.
6. For a multi-instance production environment, replace in-process Caffeine caching with a shared cache such as Redis if cross-instance cache consistency is required.
