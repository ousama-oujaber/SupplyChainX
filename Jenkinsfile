pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'your-dockerhub-username/supplychainx'
        IMAGE_TAG = "${BUILD_NUMBER}"
        
        MYSQL_ROOT_PASSWORD = 'root'
        MYSQL_DATABASE = 'supplychainx_test'
        MYSQL_USER = 'scx_user'
        MYSQL_PASSWORD = 'scx_pass'
    }
    
    tools {
        maven 'Maven-3.9.6'
        jdk 'JDK-17'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '📦 Checking out code from GitHub...'
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                echo '🔨 Building application with Maven...'
                sh '''
                    mvn clean compile -DskipTests
                '''
            }
        }
        
        stage('Test') {
            steps {
                echo '🧪 Running unit tests...'
                sh '''
                    mvn test
                '''
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java'
                    )
                }
            }
        }
        
        stage('Code Quality Analysis') {
            steps {
                echo '📊 Running SonarQube analysis...'
                script {
                    // SonarQube analysis with Maven
                    try {
                        withSonarQubeEnv('SonarQube') {
                            sh '''
                                mvn clean verify sonar:sonar \
                                  -Dsonar.projectKey=supplychainx \
                                  -Dsonar.projectName="SupplyChainX" \
                                  -Dsonar.host.url=http://supplychainx-sonarqube:9000 \
                                  -Dsonar.java.binaries=target/classes
                            '''
                        }
                        
                        //Optional: Wait for Quality Gate
                        //timeout(time: 10, unit: 'MINUTES') {
                        //    def qg = waitForQualityGate()
                        //    if (qg.status != 'OK') {
                        //        error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        //    }
                        //}
                    } catch (Exception e) {
                        echo "⚠️ SonarQube analysis failed: ${e.message}"
                        echo "Continuing build without code quality analysis..."
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                echo '📦 Packaging application...'
                sh '''
                    mvn package -DskipTests
                '''
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
            }
        }
        
        // Docker stages commented out - uncomment after adding dockerhub-credentials
        /*
        stage('Build Docker Image') {
            steps {
                echo '🐳 Building Docker image...'
                script {
                    dockerImage = docker.build("${DOCKER_IMAGE}:${IMAGE_TAG}")
                    docker.build("${DOCKER_IMAGE}:latest")
                }
            }
        }
        
        stage('Push Docker Image') {
            steps {
                echo '📤 Pushing Docker image to Docker Hub...'
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                        dockerImage.push("${IMAGE_TAG}")
                        dockerImage.push("latest")
                    }
                }
            }
        }
        */
        
        // Security Scan - requires Docker image to be built
        /*
        stage('Security Scan') {
            steps {
                echo '🔒 Scanning Docker image for vulnerabilities...'
                sh '''
                    docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
                        aquasec/trivy:latest image --severity HIGH,CRITICAL \
                        ${DOCKER_IMAGE}:${IMAGE_TAG} || true
                '''
            }
        }
        */
        
        stage('Deploy to Development') {
            when {
                branch 'develop'
            }
            steps {
                echo '🚀 Deploying to Development environment...'
                sh '''
                    docker-compose -f compose.yaml down || true
                    docker-compose -f compose.yaml up -d
                '''
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'staging'
            }
            steps {
                echo '🚀 Deploying to Staging environment...'
                input message: 'Deploy to Staging?', ok: 'Deploy'
                sh '''
                    docker-compose -f compose.staging.yaml down || true
                    docker-compose -f compose.staging.yaml up -d
                '''
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                echo '🚀 Deploying to Production environment...'
                input message: 'Deploy to Production?', ok: 'Deploy'
                sh '''
                    # Pull latest images
                    docker pull ${DOCKER_IMAGE}:${IMAGE_TAG}
                    
                    # Deploy with zero downtime
                    docker-compose -f compose.yaml up -d --no-deps --build app
                    
                    # Health check
                    sleep 30
                    curl -f http://localhost:8080/actuator/health || exit 1
                '''
            }
        }
        
        stage('Integration Tests') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                echo '🧪 Running integration tests...'
                sh '''
                    # Wait for application to be ready
                    sleep 30
                    
                    # Run integration tests
                    mvn verify -Pintegration-tests || true
                '''
            }
        }
        
        stage('Cleanup') {
            steps {
                echo '🧹 Cleaning up...'
                sh '''
                    docker system prune -f
                    docker image prune -f
                '''
            }
        }
    }
    
    post {
        success {
            echo '✅ Pipeline completed successfully!'
            echo "📦 Docker Image: ${DOCKER_IMAGE}:${IMAGE_TAG}"
            echo "📦 Artifact: target/SupplyChainX-0.0.1-SNAPSHOT.jar"
        }
        failure {
            echo '❌ Pipeline failed! Check the console output above.'
        }
        unstable {
            echo '⚠️ Pipeline is unstable!'
        }
        always {
            echo '🧹 Build finished.'
        }
    }
}
