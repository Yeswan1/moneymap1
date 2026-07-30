# GitHub Actions Scripts

This directory contains utility scripts used by the GitHub Actions CI/CD pipeline.

## Scripts

### `generate-pages.py`

Generates GitHub Pages HTML files for the E2E test report deployment.

**Purpose:**
- Creates the main landing page (`index.html`) with test execution summary
- Generates the build history index (`history/index.html`) listing all previous reports
- Dynamically adjusts content based on test execution status

**Usage:**
```bash
python3 generate-pages.py
```

**Environment Variables Required:**
- `GH_RUN_NUMBER` - GitHub Actions run number
- `GH_REF_NAME` - Branch name
- `GH_SHA` - Commit SHA
- `GH_REPOSITORY` - Repository full name (owner/repo)
- `GH_REPO_OWNER` - Repository owner
- `GH_RUN_ID` - GitHub Actions run ID

**Output:**
- `deploy_site/index.html` - Main landing page with auto-redirect to latest report
- `deploy_site/reports/history/index.html` - Build history page

**Called by:**
- `.github/workflows/android-e2e.yml` in Stage 20a (Prepare GitHub Pages Deploy Structure)
