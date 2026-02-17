# GitHub Workflows

This directory contains GitHub Actions workflows for CI/CD automation.

## Workflows

### 1. CI Pipeline (`ci.yml`)

Comprehensive continuous integration pipeline that runs on every push and pull request.

**Jobs:**

#### Backend
- **backend-lint**: Kotlin code linting and style checks
- **backend-build**: Gradle build and unit tests
- **backend-security**: OWASP dependency security scan

#### Frontend
- **frontend-lint**: ESLint and Prettier checks
- **frontend-build**: Angular build and Karma unit tests
- **frontend-security**: npm audit for vulnerabilities

#### Docker
- **docker-build**: Build Docker images for backend and frontend
- **docker-compose-test**: Test full stack with docker-compose

#### Code Quality
- **markdown-lint**: Lint documentation files
- **dependency-review**: Review dependency changes in PRs
- **codeql-analysis**: CodeQL security analysis for JavaScript and Java

#### Status
- **ci-success**: Final job that verifies all checks passed

**Triggers:**
- Push to `main`, `develop`, or `copilot/**` branches
- Pull requests to `main` or `develop`

### 2. Release (`release.yml`)

Automated release workflow for versioned releases.

**Jobs:**
- **create-release**: Creates GitHub release from git tags
- **build-and-publish**: Builds and publishes Docker images to Docker Hub

**Triggers:**
- Push of tags matching `v*` (e.g., `v1.0.0`)

**Configuration Required:**
- Docker Hub credentials in GitHub Secrets:
  - `DOCKER_USERNAME`
  - `DOCKER_PASSWORD`

### 3. Dependency Updates (`dependency-updates.yml`)

Weekly dependency check and security audit.

**Jobs:**
- **backend-dependencies**: Check Gradle dependencies for updates
- **frontend-dependencies**: Check npm dependencies for updates
- **security-audit**: Run security audits on all dependencies

**Triggers:**
- Scheduled: Every Monday at 9:00 AM UTC
- Manual: via workflow_dispatch

## Configuration Files

### Dependabot (`dependabot.yml`)

Automated dependency updates configuration:
- **Gradle** (Backend): Weekly updates for Java dependencies
- **npm** (Frontend): Weekly updates for Node.js dependencies
- **Docker**: Weekly updates for base images
- **GitHub Actions**: Weekly updates for workflow actions

Settings:
- Maximum 5 PRs for backend/frontend
- Maximum 3 PRs for Docker/GitHub Actions
- Reviewers automatically assigned
- Conventional commit messages

### Markdown Lint (`.markdownlint.json`)

Configuration for markdown linting:
- Line length: 120 characters
- Allows inline HTML
- Allows duplicate headers in different sections

### PR Template (`PULL_REQUEST_TEMPLATE.md`)

Standard template for pull requests including:
- Description and type of change
- Testing checklist
- Security considerations
- Documentation updates

### Issue Templates (`ISSUE_TEMPLATE/`)

- **bug_report.md**: Template for bug reports
- **feature_request.md**: Template for feature requests

## Usage

### Running CI Locally

#### Backend Tests
```bash
cd backend
./gradlew test
./gradlew check
```

#### Frontend Tests
```bash
cd frontend
npm test
npm run lint
```

#### Docker Build
```bash
docker-compose build
docker-compose up -d
```

### Creating a Release

1. Tag your commit:
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

2. The release workflow will automatically:
   - Create a GitHub release
   - Build Docker images
   - Push to Docker Hub with version tag and `latest`

### Triggering Dependency Check

Manually trigger from GitHub Actions UI:
1. Go to Actions → Dependency Updates
2. Click "Run workflow"
3. Select branch and run

## Secrets Required

Configure these secrets in GitHub repository settings:

- `DOCKER_USERNAME`: Docker Hub username (for releases)
- `DOCKER_PASSWORD`: Docker Hub password/token (for releases)
- `SONAR_TOKEN`: SonarCloud token (optional, if enabled)

## Badge Status

Add these badges to your README.md:

```markdown
![CI Pipeline](https://github.com/benjiiDELPECH/doc-graph-toolskit/workflows/CI%20Pipeline/badge.svg)
![Security](https://github.com/benjiiDELPECH/doc-graph-toolskit/workflows/CodeQL/badge.svg)
```

## Customization

### Adjusting CI Checks

Edit `.github/workflows/ci.yml`:
- Add/remove jobs
- Modify test commands
- Adjust failure conditions

### Adding New Checks

Create a new workflow file:
```yaml
name: My Custom Check
on:
  push:
    branches: [ main ]
jobs:
  my-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run my check
        run: echo "Running custom check"
```

### Modifying Dependabot

Edit `.github/dependabot.yml`:
- Change update frequency
- Ignore specific dependencies
- Adjust PR limits

## Best Practices

1. **Keep workflows fast**: Use caching and parallel jobs
2. **Fail fast**: Run quick checks before expensive ones
3. **Cache dependencies**: Use setup actions with caching
4. **Use artifacts**: Share build outputs between jobs
5. **Monitor costs**: GitHub Actions has usage limits

## Troubleshooting

### Workflow Fails on Fork

Some features require secrets or permissions:
- Disable SonarCloud job if not configured
- Disable Docker Hub publishing in forks
- Use `if: github.repository == 'owner/repo'` for protected jobs

### Slow Builds

- Enable caching for Gradle and npm
- Use BuildKit for Docker
- Run jobs in parallel
- Use smaller runners if possible

### Security Scans Fail

- Review and fix actual vulnerabilities
- Use `continue-on-error: true` for non-blocking scans
- Configure exceptions in dependabot.yml

## References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Dependabot Documentation](https://docs.github.com/en/code-security/dependabot)
- [CodeQL Documentation](https://codeql.github.com/docs/)
