/**
 * MoneyMap API Load Test — k6 Script
 *
 * Six scenarios:
 *   baseline   — 100 VUs, 60s constant
 *   stress200  — ramp to 200 VUs, 60s hold
 *   stress500  — ramp to 500 VUs, 60s hold
 *   stress1000 — ramp to 1000 VUs, 60s hold
 *   spike      — 50→500 VUs in 10s, hold 30s, ramp back
 *   endurance  — 100 VUs, 1800s (30 min)
 *
 * Usage:
 *   k6 run k6-load-test.js
 *   k6 run --env BASE_URL=https://myserver.com/api/v1 k6-load-test.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// ── Configuration ─────────────────────────────────────────────────────────────

const BASE_URL = __ENV.BASE_URL || 'http://localhost:3000/api/v1';
const LOGIN_EMAIL = __ENV.LOGIN_EMAIL || 'demo@moneymap.com';
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || 'Password123!';

// ── Custom metrics ─────────────────────────────────────────────────────────────

const loginDuration = new Trend('login_duration', true);
const txDuration    = new Trend('transaction_duration', true);
const errorRate     = new Rate('errors');

// ── Scenarios & Thresholds ────────────────────────────────────────────────────

export const options = {
  scenarios: {
    baseline: {
      executor: 'constant-vus',
      vus: 100,
      duration: '60s',
      tags: { scenario: 'baseline' },
    },
    stress200: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 200 },
        { duration: '60s', target: 200 },
        { duration: '10s', target: 0 },
      ],
      tags: { scenario: 'stress200' },
    },
    stress500: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 500 },
        { duration: '60s', target: 500 },
        { duration: '10s', target: 0 },
      ],
      tags: { scenario: 'stress500' },
    },
    stress1000: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 1000 },
        { duration: '60s', target: 1000 },
        { duration: '10s', target: 0 },
      ],
      tags: { scenario: 'stress1000' },
    },
    spike: {
      executor: 'ramping-vus',
      startVUs: 50,
      stages: [
        { duration: '10s', target: 500 },
        { duration: '30s', target: 500 },
        { duration: '10s', target: 50 },
      ],
      tags: { scenario: 'spike' },
    },
    endurance: {
      executor: 'constant-vus',
      vus: 100,
      duration: '1800s',
      tags: { scenario: 'endurance' },
    },
  },

  thresholds: {
    // Baseline P95 must be ≤ 500ms
    'http_req_duration{scenario:baseline}':   ['p(95)<500'],
    // Stress 200 P95 must be ≤ 1000ms
    'http_req_duration{scenario:stress200}':  ['p(95)<1000'],
    // Error rate ≤ 1% across all scenarios
    'http_req_failed{scenario:baseline}':     ['rate<0.01'],
    'http_req_failed{scenario:stress200}':    ['rate<0.01'],
    'http_req_failed{scenario:stress500}':    ['rate<0.01'],
    'http_req_failed{scenario:stress1000}':   ['rate<0.01'],
    'http_req_failed{scenario:spike}':        ['rate<0.01'],
    'http_req_failed{scenario:endurance}':    ['rate<0.01'],
  },
};

// ── Auth Helper ───────────────────────────────────────────────────────────────

/**
 * Performs login and returns the JWT access token.
 * Called once at the start of each VU lifecycle.
 */
function getAuthToken() {
  const res = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ email: LOGIN_EMAIL, password: LOGIN_PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  loginDuration.add(res.timings.duration);

  const ok = check(res, {
    'login status 200 or 201': (r) => r.status === 200 || r.status === 201,
    'login has accessToken':   (r) => {
      try {
        const body = JSON.parse(r.body);
        return !!(body.data && body.data.accessToken) || !!body.accessToken;
      } catch (_) { return false; }
    },
  });

  if (!ok) {
    errorRate.add(1);
    return null;
  }

  errorRate.add(0);
  try {
    const body = JSON.parse(res.body);
    return (body.data && body.data.accessToken) || body.accessToken || null;
  } catch (_) {
    return null;
  }
}

// ── Default VU Function ───────────────────────────────────────────────────────

export default function () {
  const token = getAuthToken();
  if (!token) {
    sleep(1);
    return;
  }

  const authHeaders = {
    'Content-Type':  'application/json',
    'Authorization': `Bearer ${token}`,
  };

  // 1. GET /transactions
  const txRes = http.get(`${BASE_URL}/transactions`, { headers: authHeaders });
  txDuration.add(txRes.timings.duration);
  check(txRes, { 'GET /transactions 200': (r) => r.status === 200 });
  errorRate.add(txRes.status !== 200 ? 1 : 0);

  sleep(0.5);

  // 2. POST /transactions (create expense)
  const txDate = new Date().toISOString().split('T')[0];
  const postTx = http.post(
    `${BASE_URL}/transactions`,
    JSON.stringify({
      amount: 100,
      type: 'EXPENSE',
      description: 'k6 load test transaction',
      transactionDate: txDate,
    }),
    { headers: authHeaders }
  );
  check(postTx, { 'POST /transactions 201': (r) => r.status === 201 || r.status === 200 });
  errorRate.add(postTx.status !== 201 && postTx.status !== 200 ? 1 : 0);

  sleep(0.3);

  // 3. GET /budgets
  const budgetsRes = http.get(`${BASE_URL}/budgets`, { headers: authHeaders });
  check(budgetsRes, { 'GET /budgets 200': (r) => r.status === 200 });
  errorRate.add(budgetsRes.status !== 200 ? 1 : 0);

  sleep(0.3);

  // 4. GET /reports/dashboard
  const reportsRes = http.get(`${BASE_URL}/reports/dashboard`, { headers: authHeaders });
  check(reportsRes, { 'GET /reports/dashboard 200': (r) => r.status === 200 });
  errorRate.add(reportsRes.status !== 200 ? 1 : 0);

  sleep(0.3);

  // 5. GET /savings-goals
  const goalsRes = http.get(`${BASE_URL}/savings-goals`, { headers: authHeaders });
  check(goalsRes, { 'GET /savings-goals 200': (r) => r.status === 200 });
  errorRate.add(goalsRes.status !== 200 ? 1 : 0);

  sleep(1);
}
