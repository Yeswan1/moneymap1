#!/usr/bin/env python3
"""
generate-pages.py — GitHub Pages index generator for MoneyMap E2E reports.

Reads deploy_site/reports/history/build-<N>/ directories and generates:
  - deploy_site/index.html                (top-level landing page)
  - deploy_site/reports/index.html        (builds listing page)

Safe on first run when history directory does not yet exist.

Usage (called from consolidate-reports job):
  python3 automation/.github-scripts/generate-pages.py
"""

import os
import re
from datetime import datetime

# ── Configuration ─────────────────────────────────────────────────────────────

DEPLOY_SITE     = "deploy_site"
HISTORY_DIR     = os.path.join(DEPLOY_SITE, "reports", "history")
LATEST_REPORT   = "reports/latest/execution-report.html"
LATEST_DASHBOARD = "reports/latest/dashboard.html"

# ── Helpers ───────────────────────────────────────────────────────────────────

def get_build_entries():
    """
    Reads build-N directories from HISTORY_DIR and returns a list of dicts
    sorted by build number descending (newest first).
    """
    if not os.path.isdir(HISTORY_DIR):
        return []

    entries = []
    for name in os.listdir(HISTORY_DIR):
        full_path = os.path.join(HISTORY_DIR, name)
        if not os.path.isdir(full_path):
            continue
        match = re.match(r'^build-(\d+)$', name)
        if not match:
            continue
        build_num = int(match.group(1))
        mtime = os.path.getmtime(full_path)
        mtime_str = datetime.fromtimestamp(mtime).strftime("%Y-%m-%d %H:%M:%S UTC")
        report_exists = os.path.exists(os.path.join(full_path, "execution-report.html"))
        json_exists   = os.path.exists(os.path.join(full_path, "execution-results.json"))
        entries.append({
            "build_num":     build_num,
            "dir_name":      name,
            "mtime_str":     mtime_str,
            "report_exists": report_exists,
            "json_exists":   json_exists,
        })

    entries.sort(key=lambda x: x["build_num"], reverse=True)
    return entries


def build_row(entry, base_path="history"):
    """Generates an HTML table row for one build entry."""
    num  = entry["build_num"]
    name = entry["dir_name"]
    ts   = entry["mtime_str"]

    report_link = (
        f'<a href="{base_path}/{name}/execution-report.html" style="color:#3B82F6">📄 HTML Report</a>'
        if entry["report_exists"] else '<span style="color:#64748B">—</span>'
    )
    json_link = (
        f'<a href="{base_path}/{name}/execution-results.json" style="color:#8B5CF6">📊 JSON</a>'
        if entry["json_exists"] else '<span style="color:#64748B">—</span>'
    )

    return (
        f"<tr>"
        f"<td style='padding:8px 12px;font-weight:600'>#{num}</td>"
        f"<td style='padding:8px 12px;color:#94A3B8'>{ts}</td>"
        f"<td style='padding:8px 12px'>{report_link}</td>"
        f"<td style='padding:8px 12px'>{json_link}</td>"
        f"</tr>"
    )


CSS = """
  <style>
    * { margin:0; padding:0; box-sizing:border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
           background:#0F172A; color:#E2E8F0; min-height:100vh; }
    .container { max-width:960px; margin:0 auto; padding:40px 24px; }
    h1 { font-size:28px; font-weight:700; color:#F8FAFC; margin-bottom:8px; }
    .subtitle { color:#94A3B8; font-size:14px; margin-bottom:32px; }
    .card { background:#1E293B; border-radius:12px; padding:24px;
            border:1px solid #334155; margin-bottom:24px; }
    .card h2 { font-size:18px; color:#F1F5F9; margin-bottom:16px; }
    .latest-btn { display:inline-block; background:#3B82F6; color:#FFF;
                  text-decoration:none; padding:12px 24px; border-radius:8px;
                  font-weight:600; font-size:15px; margin-bottom:8px; }
    .latest-btn:hover { background:#2563EB; }
    .dashboard-btn { display:inline-block; background:#8B5CF6; color:#FFF;
                     text-decoration:none; padding:12px 24px; border-radius:8px;
                     font-weight:600; font-size:15px; margin-left:12px; }
    table { width:100%; border-collapse:collapse; }
    th { background:#0F172A; color:#94A3B8; font-size:12px; text-transform:uppercase;
         letter-spacing:.05em; padding:8px 12px; text-align:left; }
    tr:nth-child(even) { background:#263148; }
    tr:hover { background:#334155; }
    .empty { color:#64748B; font-style:italic; padding:16px; }
  </style>
"""


def write_index_html(entries):
    """Writes deploy_site/index.html."""
    os.makedirs(DEPLOY_SITE, exist_ok=True)

    rows = "".join(build_row(e, base_path="reports/history") for e in entries)
    if not rows:
        rows = "<tr><td colspan='4' class='empty'>No historical builds yet.</td></tr>"

    count_str = f"{len(entries)} build(s)" if entries else "No builds yet"

    html = f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>MoneyMap E2E Test Reports</title>
  {CSS}
</head>
<body>
  <div class="container">
    <h1>📱 MoneyMap E2E Test Reports</h1>
    <p class="subtitle">Android Appium E2E Automation — GitHub Pages Report Hub</p>

    <div class="card">
      <h2>🚀 Latest Report</h2>
      <a href="{LATEST_REPORT}" class="latest-btn">📄 Open Latest Execution Report</a>
      <a href="{LATEST_DASHBOARD}" class="dashboard-btn">📊 Open Dashboard</a>
    </div>

    <div class="card">
      <h2>📜 Build History ({count_str})</h2>
      <table>
        <thead>
          <tr>
            <th>Build</th><th>Timestamp</th><th>HTML Report</th><th>JSON Results</th>
          </tr>
        </thead>
        <tbody>
          {rows}
        </tbody>
      </table>
    </div>
  </div>
</body>
</html>"""

    out_path = os.path.join(DEPLOY_SITE, "index.html")
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(html)
    print(f"Written: {out_path}")


def write_reports_index_html(entries):
    """Writes deploy_site/reports/index.html."""
    reports_dir = os.path.join(DEPLOY_SITE, "reports")
    os.makedirs(reports_dir, exist_ok=True)

    rows = "".join(build_row(e, base_path="history") for e in entries)
    if not rows:
        rows = "<tr><td colspan='4' class='empty'>No historical builds yet.</td></tr>"

    count_str = f"{len(entries)} build(s)" if entries else "No builds yet"

    html = f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>MoneyMap — Reports Index</title>
  {CSS}
</head>
<body>
  <div class="container">
    <h1>📊 Test Execution Reports</h1>
    <p class="subtitle"><a href="../index.html" style="color:#3B82F6">← Back to Home</a></p>

    <div class="card">
      <h2>🚀 Latest Run</h2>
      <a href="latest/execution-report.html" class="latest-btn">📄 Latest Report</a>
      <a href="latest/dashboard.html" class="dashboard-btn">📊 Dashboard</a>
    </div>

    <div class="card">
      <h2>📜 All Builds ({count_str})</h2>
      <table>
        <thead>
          <tr>
            <th>Build</th><th>Timestamp</th><th>HTML Report</th><th>JSON Results</th>
          </tr>
        </thead>
        <tbody>
          {rows}
        </tbody>
      </table>
    </div>
  </div>
</body>
</html>"""

    out_path = os.path.join(reports_dir, "index.html")
    with open(out_path, "w", encoding="utf-8") as f:
        f.write(html)
    print(f"Written: {out_path}")


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    print("generate-pages.py: Building GitHub Pages index files...")

    if not os.path.isdir(HISTORY_DIR):
        print(f"  History dir not found ({HISTORY_DIR}) — generating empty-state pages.")
    else:
        builds = [d for d in os.listdir(HISTORY_DIR) if re.match(r'^build-\d+$', d)]
        print(f"  Found {len(builds)} build directory(s) in history.")

    entries = get_build_entries()
    write_index_html(entries)
    write_reports_index_html(entries)
    print(f"generate-pages.py: Done. Generated index pages for {len(entries)} build(s).")


if __name__ == "__main__":
    main()
