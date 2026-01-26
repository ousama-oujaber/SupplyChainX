# Quick Start: Docker Hub CI/CD

## 🎯 What You Need to Do

### 1️⃣ Get Your Docker Hub Token

1. Go to https://hub.docker.com
2. Click your profile → **Account Settings** → **Security**
3. Click **New Access Token**
4. Name: `github-actions-supplychainx`
5. Permissions: **Read, Write, Delete**
6. **Copy the token** (shown only once!)

### 2️⃣ Add Secrets to GitHub

1. Go to your GitHub repository
2. **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret** and add:

```
Name: DOCKER_HUB_USERNAME
Value: your-dockerhub-username

Name: DOCKER_HUB_ACCESS_TOKEN  
Value: paste-your-token-here
```

### 3️⃣ Push Your Code

```bash
git add .
git commit -m "Add Docker Hub CI/CD pipeline"
git push origin backend  # or 'git push origin main'
```

### 4️⃣ Watch It Work! 

Go to the **Actions** tab in your GitHub repository and watch your image build automatically! 🎉

**Note:** The workflow triggers on both `main` and `backend` branches.

---

## 📦 Using Your Published Image

Once the workflow completes:

```bash
# Pull your image
docker pull <your-username>/supplychainx:latest

# Run it (with your database)
docker run -d \
  --name supplychainx-app \
  --network host \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/supplychainx \
  -e SPRING_DATASOURCE_USERNAME=scx_user \
  -e SPRING_DATASOURCE_PASSWORD=scx_pass \
  -e SPRING_LIQUIBASE_URL=jdbc:mysql://127.0.0.1:3306/supplychainx \
  -e SPRING_LIQUIBASE_USER=scx_user \
  -e SPRING_LIQUIBASE_PASSWORD=scx_pass \
  <your-username>/supplychainx:latest
```

---

## 🏷️ Creating a Release

```bash
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

This creates images tagged as: `v1.0.0`, `v1.0`, `v1`, and `latest`

---

## 📚 Full Documentation

- **Deployment Guide**: [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)
- **Walkthrough**: See walkthrough.md artifact
- **Workflow Details**: [.github/README.md](.github/README.md)

---

## ✅ What Was Done

- ✅ Multi-stage Dockerfile (builds in CI/CD automatically)
- ✅ GitHub Actions workflow (auto-build on push)
- ✅ Multi-platform support (amd64, arm64)
- ✅ Security (non-root user, token auth)
- ✅ Optimized (253MB image, 99% smaller build context)

**You're all set!** Just add the secrets and push to GitHub. 🚀
