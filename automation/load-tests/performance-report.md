# MoneyMap API Load Test Performance Report

## Test Configuration

| Property | Value |
|---|---|
| Target URL | http://localhost:3000/api/v1 |
| Tool | k6 v0.52+ |
| Date | YYYY-MM-DD HH:MM:SS |
| Environment | CI / Local |
| Test User | demo@moneymap.com |
| Backend | NestJS + PostgreSQL 16 + Redis 7 |

---

## Scenario Results

| Scenario | VUs | Duration | RPS | Avg (ms) | Min (ms) | Max (ms) | P95 (ms) | P99 (ms) | Error Rate | P95 Threshold | Status |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Baseline | 100 | 60s | — | — | — | — | — | — | — | ≤ 500ms | ⏳ Pending |
| Stress 200 | 200 | 60s | — | — | — | — | — | — | — | ≤ 1000ms | ⏳ Pending |
| Stress 500 | 500 | 60s | — | — | — | — | — | — | — | — | ⏳ Pending |
| Stress 1000 | 1000 | 60s | — | — | — | — | — | — | — | — | ⏳ Pending |
| Spike | 50→500 | 50s | — | — | — | — | — | — | — | error ≤ 1% | ⏳ Pending |
| Endurance | 100 | 1800s | — | — | — | — | — | — | — | — | ⏳ Pending |

*Fill this table after running: `k6 run automation/load-tests/k6-load-test.js`*

---

## Threshold Assessment

| Scenario | Rule | Actual P95 | Result |
|---|---|---|---|
| Baseline | P95 ≤ 500ms | — | ⏳ Pending |
| Stress 200 | P95 ≤ 1000ms | — | ⏳ Pending |
| All scenarios | Error rate ≤ 1% | — | ⏳ Pending |

> **Note:** Individual scenario threshold breaches do not fail the CI pipeline — they produce warning
> annotations in the GitHub Actions step summary only.

---

## Baseline Load Test Example Output

When running against a healthy local environment, expect results similar to:

```
Requests per second:   ~120 req/sec
Response Times:
  Average:  250 ms
  Minimum:   50 ms
  Maximum: 1500 ms
  P95:      480 ms
  P99:      950 ms
Error Rate: 0.2%
```

---

## Observations

### Memory Behaviour
*Populate after endurance run — monitor heap usage over 30 min.*

### CPU Usage
*Populate after stress runs — watch for saturation at 500/1000 VU levels.*

### Connection Pool Saturation
*Check Prisma connection pool exhaustion. Default pool size: 10. Consider increasing for stress scenarios.*

### Redis Cache Performance
*Monitor Redis hit rate — low hit rate can increase P95 latency significantly.*

---

## Recommendations

*Populate after identifying bottlenecks from scenario results.*

1. **Connection Pool:** If stress scenarios show high latency, increase `DATABASE_URL?connection_limit=20`.
2. **Redis Caching:** Add caching for `GET /transactions` and `GET /reports/dashboard` to reduce DB load.
3. **Rate Limiting:** Current throttling is set per-IP — consider per-user limits for production.
4. **Horizontal Scaling:** If P95 > 1000ms under 500 VU stress, consider load balancing across 2+ instances.

---

## How to Run

```bash
# Install k6
brew install k6       # macOS
choco install k6      # Windows

# Run all scenarios
k6 run automation/load-tests/k6-load-test.js

# Run with custom target
k6 run --env BASE_URL=http://myserver.com/api/v1 automation/load-tests/k6-load-test.js

# Artillery
npx artillery run automation/load-tests/artillery-load-test.yml

# JMeter (GUI)
jmeter -t automation/load-tests/jmeter-test-plan.jmx

# JMeter (headless)
jmeter -n -t automation/load-tests/jmeter-test-plan.jmx -l jmeter-results.jtl
```
