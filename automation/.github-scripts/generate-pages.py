#!/usr/bin/env python3
"""
GitHub Pages HTML Generator for MoneyMap E2E Reports
Generates index.html and history index from test execution results
"""

import os
import glob
import sys

def main():
    # Read environment variables
    build_number = os.environ.get("GH_RUN_NUMBER", "?")
    branch = os.environ.get("GH_REF_NAME", "main")
    commit_sha = os.environ.get("GH_SHA", "")[:8]
    repository = os.environ.get("GH_REPOSITORY", "")
    owner = os.environ.get("GH_REPO_OWNER", "")
    run_id = os.environ.get("GH_RUN_ID", "")
    
    repo_name = repository.split("/")[-1] if "/" in repository else repository
    actions_url = f"https://github.com/{repository}/actions/runs/{run_id}"
    
    # Check if tests completed successfully
    report_exists = os.path.exists("automation/Test Results/HTML/execution-report.html")
    
    # Status-based variables
    badge_color = "#10B981" if report_exists else "#EF4444"
    badge_text = "Tests Completed" if report_exists else "Pipeline Incomplete"
    message = "Redirecting to latest report in 3 seconds..." if report_exists else "Tests did not complete. Check pipeline logs."
    redirect_meta = '<meta http-equiv="refresh" content="3; url=reports/latest/execution-report.html">' if report_exists else ""
    cta_link = '<a href="reports/latest/execution-report.html">View Execution Report</a>' if report_exists else f'<a href="{actions_url}" target="_blank">View Pipeline Logs</a>'
    
    # Generate main index.html
    index_html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    {redirect_meta}
    <title>MoneyMap E2E Reports - Build {build_number}</title>
    <style>
        * {{
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }}
        body {{
            background: #0F172A;
            color: #F8FAFC;
            font-family: system-ui, -apple-system, sans-serif;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
        }}
        .card {{
            background: #1E293B;
            border: 1px solid #334155;
            border-radius: 20px;
            padding: 48px 40px;
            text-align: center;
            max-width: 520px;
            width: 90%;
        }}
        h1 {{
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 10px;
            color: #60A5FA;
        }}
        .badge {{
            display: inline-block;
            background: {badge_color};
            color: #fff;
            font-size: 12px;
            font-weight: 600;
            padding: 4px 12px;
            border-radius: 999px;
            margin-bottom: 18px;
        }}
        .meta {{
            color: #64748B;
            font-size: 13px;
            margin-bottom: 20px;
            line-height: 1.9;
        }}
        .meta b {{
            color: #94A3B8;
        }}
        p {{
            color: #94A3B8;
            font-size: 14px;
            margin-bottom: 24px;
        }}
        a {{
            background: #3B82F6;
            color: #fff;
            text-decoration: none;
            padding: 11px 26px;
            border-radius: 10px;
            font-weight: 600;
            display: inline-block;
            font-size: 14px;
        }}
        a:hover {{
            background: #2563EB;
        }}
    </style>
</head>
<body>
    <div class="card">
        <h1>MoneyMap E2E Reports</h1>
        <div class="badge">{badge_text}</div>
        <div class="meta">
            Build <b>#{build_number}</b> &middot; Branch <b>{branch}</b><br>
            Commit <b>{commit_sha}</b>
        </div>
        <p>{message}</p>
        {cta_link}
    </div>
</body>
</html>"""
    
    with open("deploy_site/index.html", "w", encoding="utf-8") as f:
        f.write(index_html)
    
    print("✓ Generated deploy_site/index.html")
    
    # Generate history index
    builds = sorted(glob.glob("deploy_site/reports/history/build-*/"), reverse=True)
    
    items = []
    for build_path in builds:
        report_file = os.path.join(build_path, "execution-report.html")
        if os.path.exists(report_file):
            build_name = os.path.basename(build_path.rstrip("/"))
            items.append(f'<li><a href="{build_name}/execution-report.html">Report: {build_name}</a></li>')
    
    if not items:
        items.append("<li>No completed reports yet</li>")
    
    history_html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MoneyMap Build History</title>
    <style>
        body {{
            background: #0F172A;
            color: #F8FAFC;
            font-family: system-ui, -apple-system, sans-serif;
            padding: 32px;
            max-width: 800px;
            margin: 0 auto;
        }}
        h1 {{
            color: #60A5FA;
            margin-bottom: 24px;
        }}
        ul {{
            list-style: none;
            padding: 0;
        }}
        li {{
            background: #1E293B;
            border: 1px solid #334155;
            border-radius: 10px;
            margin: 8px 0;
            padding: 14px 20px;
        }}
        a {{
            color: #60A5FA;
            text-decoration: none;
            font-weight: 600;
        }}
        a:hover {{
            text-decoration: underline;
        }}
    </style>
</head>
<body>
    <h1>MoneyMap Build History</h1>
    <ul>
        {"".join(items)}
    </ul>
    <p style="margin-top: 20px;">
        <a href="../latest/execution-report.html">Latest Report</a>
    </p>
</body>
</html>"""
    
    with open("deploy_site/reports/history/index.html", "w", encoding="utf-8") as f:
        f.write(history_html)
    
    print("✓ Generated deploy_site/reports/history/index.html")
    print("✓ HTML generation completed successfully")

if __name__ == "__main__":
    main()
