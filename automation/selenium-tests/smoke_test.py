"""
MoneyMap live-site Selenium smoke suite.

Unlike the old workflow step this replaces, every result here comes from an
actual headless Chrome session hitting LIVE_URL. Nothing is hardcoded to
PASSED. If the site is unreachable or an element/assertion is missing, the
corresponding test is recorded as FAILED and the process exits non-zero so
the CI job actually fails.
"""

import json
import os
import sys
import datetime

from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, WebDriverException

LIVE_URL = os.environ.get("LIVE_URL", "").rstrip("/")
OUT_DIR = "selenium-results"
TIMEOUT = 15


def make_driver():
    opts = Options()
    opts.add_argument("--headless=new")
    opts.add_argument("--no-sandbox")
    opts.add_argument("--disable-dev-shm-usage")
    opts.add_argument("--window-size=1366,900")
    return webdriver.Chrome(options=opts)


def run_case(driver, test_id, name, fn):
    started = datetime.datetime.utcnow()
    try:
        fn(driver)
        status, error = "PASSED", None
    except (AssertionError, TimeoutException, WebDriverException) as e:
        status, error = "FAILED", str(e)[:300]
    duration_ms = int((datetime.datetime.utcnow() - started).total_seconds() * 1000)
    return {
        "testId": test_id,
        "name": name,
        "status": status,
        "durationMs": duration_ms,
        "error": error,
    }


# ---- actual checks against real DOM elements from website/index.html ----

def check_page_loads(driver):
    driver.get(LIVE_URL)
    WebDriverWait(driver, TIMEOUT).until(lambda d: d.execute_script("return document.readyState") == "complete")


def check_title(driver):
    assert "MoneyMap" in driver.title, f"unexpected title: {driver.title!r}"


def check_auth_view_visible(driver):
    el = WebDriverWait(driver, TIMEOUT).until(EC.presence_of_element_located((By.ID, "authView")))
    assert el.is_displayed(), "authView is not visible on load"


def check_login_form_present(driver):
    WebDriverWait(driver, TIMEOUT).until(EC.presence_of_element_located((By.ID, "loginForm")))
    driver.find_element(By.ID, "loginEmail")
    driver.find_element(By.ID, "loginPassword")


def check_signup_form_present(driver):
    driver.find_element(By.ID, "signupForm")
    driver.find_element(By.ID, "signupEmail")
    driver.find_element(By.ID, "signupPassword")


def check_no_console_errors_on_load(driver):
    logs = driver.get_log("browser")
    severe = [l for l in logs if l.get("level") == "SEVERE"]
    assert not severe, f"{len(severe)} SEVERE console errors, e.g. {severe[0]['message'][:150]}"


TEST_CASES = [
    ("TC_SEL_001", "Site responds and DOM finishes loading", check_page_loads),
    ("TC_SEL_002", "Page title contains MoneyMap", check_title),
    ("TC_SEL_003", "Auth view is visible on initial load", check_auth_view_visible),
    ("TC_SEL_004", "Login form fields are present", check_login_form_present),
    ("TC_SEL_005", "Signup form fields are present", check_signup_form_present),
    ("TC_SEL_006", "No severe JS console errors on load", check_no_console_errors_on_load),
]


def main():
    os.makedirs(f"{OUT_DIR}/HTML", exist_ok=True)
    os.makedirs(f"{OUT_DIR}/JSON", exist_ok=True)
    os.makedirs(f"{OUT_DIR}/Summary", exist_ok=True)

    if not LIVE_URL:
        print("LIVE_URL not set — cannot run tests")
        sys.exit(1)

    driver = make_driver()
    results = []
    try:
        for test_id, name, fn in TEST_CASES:
            result = run_case(driver, test_id, name, fn)
            print(f"[{result['status']}] {test_id} - {name}" + (f" :: {result['error']}" if result["error"] else ""))
            results.append(result)
    finally:
        driver.quit()

    total = len(results)
    passed = sum(1 for r in results if r["status"] == "PASSED")
    failed = total - passed

    report = {
        "deploymentUrl": LIVE_URL,
        "executedAt": datetime.datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S UTC"),
        "total": total,
        "passed": passed,
        "failed": failed,
        "testCases": results,
    }
    with open(f"{OUT_DIR}/JSON/execution-results.json", "w") as f:
        json.dump(report, f, indent=2)

    with open(f"{OUT_DIR}/Summary/summary.md", "w") as f:
        f.write("# MoneyMap Selenium Smoke Summary\n\n")
        f.write(f"| Metric | Value |\n|---|---|\n")
        f.write(f"| Deployment URL | {LIVE_URL} |\n")
        f.write(f"| Total | {total} |\n")
        f.write(f"| Passed | {passed} |\n")
        f.write(f"| Failed | {failed} |\n")

    # Fail the CI step for real if anything actually failed.
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()