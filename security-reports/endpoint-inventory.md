# API Endpoint Inventory — MoneyMap Backend

Base URL: `http://localhost:3000/api/v1`  
Swagger Docs: `http://localhost:3000/api/v1/docs`

## Authentication Endpoints (Public)

| Method | Path | Auth Required | Description |
|---|---|---|---|
| POST | /auth/register | No | Create new user account |
| POST | /auth/login | No | Email/password login → returns JWT |
| POST | /auth/google | No | Google OAuth2 sign-in |
| POST | /auth/refresh | Refresh token | Rotate access token |
| POST | /auth/logout | Bearer JWT | Revoke refresh token |

## User Endpoints (Protected)

| Method | Path | Auth Required | Description |
|---|---|---|---|
| GET | /users/me | Bearer JWT | Get current user profile |
| PATCH | /users/me | Bearer JWT | Update user profile |
| POST | /users/me/profile | Bearer JWT | Complete role-specific profile setup |
| DELETE | /users/me | Bearer JWT | Soft-delete user account |

## Transaction Endpoints (Protected)

| Method | Path | Auth Required | Description |
|---|---|---|---|
| GET | /transactions | Bearer JWT | List transactions (paginated, filterable) |
| POST | /transactions | Bearer JWT | Create transaction |
| GET | /transactions/:id | Bearer JWT | Get single transaction |
| PATCH | /transactions/:id | Bearer JWT | Update transaction |
| DELETE | /transactions/:id | Bearer JWT | Soft-delete transaction |

## Budget Endpoints (Protected)

| Method | Path | Auth Required | Description |
|---|---|---|---|
| GET | /budgets | Bearer JWT | Get budgets for month/year |
| POST | /budgets | Bearer JWT | Set/update budget for category |
| DELETE | /budgets/:id | Bearer JWT | Delete budget entry |

## Reports Endpoints (Protected)

| Method | Path | Auth Required | Description |
|---|---|---|---|
| GET | /reports/dashboard | Bearer JWT | Dashboard summary (income/expense totals) |
| GET | /reports/monthly | Bearer JWT | Monthly breakdown by category |
| GET | /reports/weekly | Bearer JWT | Weekly spending report |

## Savings Goals Endpoints (Protected)

| Method | Path | Auth Required | Description |
|---|---|---|---|
| GET | /savings-goals | Bearer JWT | List savings goals |
| POST | /savings-goals | Bearer JWT | Create savings goal |
| PATCH | /savings-goals/:id | Bearer JWT | Update goal progress |
| DELETE | /savings-goals/:id | Bearer JWT | Delete savings goal |

## Subscriptions Endpoints (Protected)

| Method | Path | Auth Required | Description |
|---|---|---|---|
| GET | /subscriptions | Bearer JWT | List subscriptions |
| POST | /subscriptions | Bearer JWT | Add subscription |
| PATCH | /subscriptions/:id | Bearer JWT | Update subscription |
| DELETE | /subscriptions/:id | Bearer JWT | Delete subscription |

## Categories Endpoints (Protected)

| Method | Path | Auth Required | Description |
|---|---|---|---|
| GET | /categories | Bearer JWT | List categories (system + user-defined) |
| POST | /categories | Bearer JWT | Create custom category |
| DELETE | /categories/:id | Bearer JWT | Delete custom category |

## Health / System Endpoints (Public)

| Method | Path | Auth Required | Description |
|---|---|---|---|
| GET | / | No | Root health check |
| GET | /api/v1/docs | No | Swagger UI documentation |
