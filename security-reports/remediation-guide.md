# MoneyMap Backend — Security Remediation Guide

> This guide provides prioritised remediation steps for findings from the automated security scan.
> Address Critical and High findings before any production deployment.

---

## Priority 1 — Critical (Fix Immediately)

### JWT Secret Entropy
**Risk:** Hardcoded or weak JWT secrets allow token forgery.
**Evidence:** `JWT_ACCESS_SECRET` and `JWT_REFRESH_SECRET` use placeholder values in `.env.example`.
**Fix:**
```bash
# Generate cryptographically strong secrets
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
```
Set the output as `JWT_ACCESS_SECRET` and `JWT_REFRESH_SECRET` in your production environment (GitHub Secret / AWS SSM / Vault). Never commit real values.

**Verification:** Run `grep -r "JWT_ACCESS_SECRET" backend/src` — should find only `process.env.JWT_ACCESS_SECRET`, never a literal string.

---

### Secrets in Repository
**Risk:** API keys, tokens, or credentials committed to Git history.
**Fix:**
```bash
# Install BFG Repo Cleaner and remove committed secrets
brew install bfg
bfg --replace-text secrets.txt MoneyMap.git
git reflog expire --expire=now --all
git gc --prune=now --aggressive
```
After cleaning history, rotate all exposed credentials immediately.

**Verification:** Re-run Gitleaks — `./gitleaks detect --source . --exit-code 1` must exit 0.

---

## Priority 2 — High

### CORS Configuration
**Risk:** Wildcard CORS (`CORS_ORIGINS=*`) in `backend/src/main.ts` allows any origin.
**Fix:**
```typescript
// backend/src/main.ts
app.enableCors({
  origin: process.env.CORS_ORIGINS?.split(',') || ['https://yourdomain.com'],
  credentials: true,
});
```
Set `CORS_ORIGINS=https://yourfrontenddomain.com` in production environment.

---

### Missing Rate Limiting on Auth Endpoints
**Risk:** Login endpoint is subject to brute-force without per-IP throttling.
**Current:** `ThrottlerModule` is imported but limits may be too permissive.
**Fix:**
```typescript
// Tighten ThrottlerModule in app.module.ts
ThrottlerModule.forRoot([{
  name: 'login',
  ttl: 60000,    // 60 seconds
  limit: 5,      // max 5 attempts per IP
}])
```
Apply `@Throttle({ login: { limit: 5, ttl: 60000 } })` to `POST /auth/login`.

---

### Refresh Token Not Rotated on Every Use
**Risk:** Stolen refresh token can be replayed indefinitely.
**Fix:** Implement refresh token rotation — invalidate the old token on every `/auth/refresh` call. The `RefreshToken` model already has `isRevoked` — ensure it is set to `true` after each use and a new token is issued.

---

## Priority 3 — Medium

### Content Security Policy (CSP) Not Set
**Risk:** XSS attacks can exfiltrate data without CSP protection.
**Fix:** Add to `main.ts` after `app.use(helmet())`:
```typescript
app.use(
  helmet.contentSecurityPolicy({
    directives: {
      defaultSrc: ["'self'"],
      scriptSrc: ["'self'"],
      styleSrc: ["'self'", "'unsafe-inline'"],
      imgSrc: ["'self'", 'data:'],
    },
  })
);
```

---

### Swagger UI Exposed in Production
**Risk:** `api/v1/docs` reveals full API schema to attackers.
**Fix:** Gate Swagger behind an environment check:
```typescript
if (process.env.NODE_ENV !== 'production') {
  SwaggerModule.setup(`${apiPrefix}/docs`, app, document);
}
```

---

### Soft-Delete Without Data Isolation
**Risk:** Soft-deleted users (`deletedAt` not null) can still have their data queried if filters are missed.
**Fix:** Add a global Prisma middleware that automatically appends `deletedAt: null` to all User queries, or use Prisma's `@@index([deletedAt])` with consistent query guards.

---

### Verbose Error Responses
**Risk:** Stack traces or internal error details leaked in API responses.
**Fix:** Review `HttpExceptionFilter` — ensure `exception.stack` is never included in the response body in production:
```typescript
// Only include message, not stack
response.json({
  statusCode: status,
  message: NODE_ENV === 'production' ? 'Internal server error' : message,
});
```

---

## Priority 4 — Low

### Dependency Updates
**Risk:** Outdated packages with known CVEs.
**Fix:**
```bash
cd backend
npx npm-check-updates -u
npm install
npm audit fix
```
Run `npm audit` after each update and resolve remaining issues.

---

### Missing HTTP Strict Transport Security (HSTS)
**Fix:** Enable HSTS via Helmet:
```typescript
app.use(helmet.hsts({ maxAge: 31536000, includeSubDomains: true }));
```

---

### Cookie Security Flags
**Risk:** Refresh token cookies without `Secure`, `HttpOnly`, `SameSite=Strict`.
**Fix:** When setting refresh token cookies, always include:
```typescript
res.cookie('refreshToken', token, {
  httpOnly: true,
  secure: process.env.NODE_ENV === 'production',
  sameSite: 'strict',
  maxAge: 7 * 24 * 60 * 60 * 1000,
});
```

---

## Verification Checklist

After applying all fixes, run the full security pipeline:

```bash
# 1. Re-run Gitleaks (should be 0 secrets)
./gitleaks detect --source . --exit-code 1

# 2. Re-run Semgrep
semgrep scan --config "p/nodejs" --config "p/typescript" backend/

# 3. Re-run Trivy
trivy fs backend/ --severity CRITICAL,HIGH

# 4. Re-run OWASP DepCheck
dependency-check.sh --project MoneyMap --scan backend --format HTML

# 5. Push to main → security-review.yml must pass ✅
```

---

## OWASP Top 10 Mapping

| OWASP Category | Finding | Status |
|---|---|---|
| A01: Broken Access Control | Soft-delete isolation | 🔶 Medium |
| A02: Cryptographic Failures | Weak JWT secret example | 🔴 Critical |
| A03: Injection | Prisma ORM — parameterized | ✅ Mitigated |
| A05: Security Misconfiguration | CORS wildcard, Swagger exposed | 🟡 High |
| A06: Vulnerable Components | Check `npm audit` | 🟡 Verify |
| A07: Auth Failures | Rate limiting, token rotation | 🟡 High |
| A09: Logging Failures | Verbose error responses | 🔶 Medium |

---

*Generated by MoneyMap Security Review Pipeline — `security-review.yml`*
