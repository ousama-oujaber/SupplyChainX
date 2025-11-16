pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'protocol404/supplychainx'
        IMAGE_TAG = "${BUILD_NUMBER}"
        
        MYSQL_ROOT_PASSWORD = 'root'
        MYSQL_DATABASE = 'supplychainx_test'
        MYSQL_USER = 'scx_user'
        MYSQL_PASSWORD = 'scx_pass'
        
        // Production Droplet Configuration
        DROPLET_IP = '64.226.103.218'
        DROPLET_USER = 'root'
        DEPLOY_DIR = '/opt/supplychainx'
        
        // Maven options for better network resilience
        MAVEN_OPTS = '-Dmaven.wagon.http.retryHandler.count=3 -Dmaven.wagon.httpconnectionManager.ttlSeconds=120'
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
        
        stage('Deploy to Production') {
            when {
                branch 'master'
            }
            steps {
                echo '🚀 Deploying to DigitalOcean Droplet...'
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: 'droplet-ssh-key', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                        sh '''
                            # Make deployment script executable
                            chmod +x deploy-to-droplet.sh
                            
                            # Setup SSH
                            mkdir -p ~/.ssh
                            cp $SSH_KEY ~/.ssh/id_rsa
                            chmod 600 ~/.ssh/id_rsa
                            
                            # Set environment variables for deployment
                            export DROPLET_IP="${DROPLET_IP}"
                            export DROPLET_USER="${DROPLET_USER}"
                            export DOCKER_IMAGE="${DOCKER_IMAGE}:${IMAGE_TAG}"
                            
                            # Run deployment script
                            ./deploy-to-droplet.sh
                        '''
                    }
                }
            }
        }
        
        stage('Smoke Test') {
            when {
                branch 'master'
            }
            steps {
                echo '🔍 Running smoke tests on production...'
                script {
                    sh '''
                        # Wait for application to be fully ready
                        echo "Waiting for application to respond..."
                        for i in {1..30}; do
                            if curl -f http://${DROPLET_IP}:8080/actuator/health; then
                                echo "✓ Application is responding!"
                                break
                            fi
                            echo "Waiting... ($i/30)"
                            sleep 5
                        done
                        
                        # Basic health check
                        HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://${DROPLET_IP}:8080/actuator/health)
                        if [ $HTTP_STATUS -eq 200 ]; then
                            echo "✓ Health check passed (HTTP $HTTP_STATUS)"
                        else
                            echo "✗ Health check failed (HTTP $HTTP_STATUS)"
                            exit 1
                        fi
                    '''
                }
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
