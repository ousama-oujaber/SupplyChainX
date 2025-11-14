pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'protocol404/supplychainx'
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
