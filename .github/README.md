# GitHub Actions Workflows

This directory contains CI/CD workflows for the SupplyChainX project.

## Workflows

### `docker-publish.yml` - Docker Build and Push

Automates building and publishing Docker images to Docker Hub.

**Triggers:**
- Push to `main` or `backend` branch → Builds and pushes images tagged with branch name
- Pull requests to `main` or `backend` → Builds only (no push)
- Git tags (e.g., `v1.0.0`) → Builds and pushes versioned images
- Manual dispatch → Via GitHub Actions UI

**Required Secrets:**
- `DOCKER_HUB_USERNAME` - Your Docker Hub username
- `DOCKER_HUB_ACCESS_TOKEN` - Docker Hub access token

**Features:**
- Multi-stage Docker build
- Multi-platform support (amd64, arm64)
- Layer caching for faster builds
- Semantic versioning support
- Automatic tagging strategy

**Image Tags:**
- `latest` - Latest build from main branch
- `<branch-name>` - Builds from specific branches
- `pr-<number>` - Pull request builds
- `v1.0.0`, `v1.0`, `v1` - Semantic version tags

For detailed setup instructions, see [docs/DEPLOYMENT.md](../docs/DEPLOYMENT.md).
