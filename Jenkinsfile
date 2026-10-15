pipeline {
    agent any

    // Cấu hình các tham số khi chạy Pipeline (Build with Parameters)
    parameters {
        choice(
            name: 'ENVIRONMENT', 
            choices: ['development', 'staging', 'production'], 
            description: 'Môi trường triển khai mục tiêu'
        )
        booleanParam(
            name: 'SKIP_TESTS', 
            defaultValue: false, 
            description: 'Bỏ qua bước Unit Test khi build'
        )
        booleanParam(
            name: 'BUILD_DOCKER_IMAGES', 
            defaultValue: true, 
            description: 'Build Docker Image cho tất cả các dịch vụ (Backend, Frontend, MLService)'
        )
        booleanParam(
            name: 'PUSH_TO_REGISTRY', 
            defaultValue: false, 
            description: 'Đẩy Docker Image lên Docker Registry'
        )
        string(
            name: 'DOCKER_REGISTRY', 
            defaultValue: 'docker.io/myusername', 
            description: 'Địa chỉ Registry / Docker Hub Namespace'
        )
        booleanParam(
            name: 'DEPLOY_APPLICATION', 
            defaultValue: false, 
            description: 'Triển khai dịch vụ bằng Docker Compose sau khi build'
        )
    }

    // Các thiết lập tối ưu hóa cho Pipeline
    options {
        buildDiscarder(logRotator(numToKeepStr: '15', artifactNumToKeepStr: '5'))
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    environment {
        // Tag phiên bản dựa trên Build Number hoặc Git Commit ngắn
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        PROJECT_DIR = "."
        // ID Credential cấu hình trong Jenkins (nếu dùng đẩy Docker image)
        // DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
    }

    stages {
        stage('Khởi tạo & Xác định Thư mục') {
            steps {
                script {
                    echo "========================================================"
                    echo "              EDUMY CI/CD PIPELINE                     "
                    echo "========================================================"
                    echo "Build Number   : ${env.BUILD_NUMBER}"
                    echo "Git Branch     : ${env.GIT_BRANCH ?: 'N/A'}"
                    echo "Môi trường     : ${params.ENVIRONMENT}"
                    echo "Skip Tests     : ${params.SKIP_TESTS}"
                    echo "Build Docker   : ${params.BUILD_DOCKER_IMAGES}"
                    echo "========================================================"

                    // Tự động nhận diện vị trí thư mục Edumy linh hoạt
                    if (fileExists('Edumy-main/Edumy/docker-compose.yml')) {
                        env.PROJECT_DIR = 'Edumy-main/Edumy'
                    } else if (fileExists('Edumy/docker-compose.yml')) {
                        env.PROJECT_DIR = 'Edumy'
                    } else {
                        env.PROJECT_DIR = '.'
                    }
                    echo "==> Đã xác định thư mục dự án: ${env.PROJECT_DIR}"
                }
            }
        }

        stage('Build & Test Các Dịch Vụ') {
            parallel {
                // 1. BACKEND: Spring Boot 3 (Java 17, Maven)
                stage('Backend (Spring Boot 3)') {
                    steps {
                        dir("${env.PROJECT_DIR}/Backend") {
                            script {
                                echo "--> [Backend] Đang biên dịch và kiểm thử Spring Boot..."
                                def testOption = params.SKIP_TESTS ? "-DskipTests" : ""

                                if (isUnix()) {
                                    sh "chmod +x mvnw"
                                    sh "./mvnw clean package ${testOption}"
                                } else {
                                    bat "mvnw.cmd clean package ${testOption}"
                                }
                            }
                        }
                    }
                    post {
                        success {
                            echo "--> [Backend] Build JAR thành công."
                            // Lưu trữ file artifact JAR vào Jenkins
                            archiveArtifacts artifacts: "${env.PROJECT_DIR}/Backend/target/*.jar", fingerprint: true, allowEmptyArchive: true
                        }
                        failure {
                            echo "--> [Backend] Lỗi biên dịch Spring Boot!"
                        }
                    }
                }

                // 2. FRONTEND: React + Vite (Node.js)
                stage('Frontend (React & Vite)') {
                    steps {
                        dir("${env.PROJECT_DIR}/Frontend") {
                            script {
                                echo "--> [Frontend] Cài đặt dependencies và build client..."
                                if (isUnix()) {
                                    sh "npm ci || npm install"
                                    // Chạy linter kiểm tra cú pháp (không dừng pipeline nếu có warning)
                                    sh "npm run lint || true"
                                    sh "npm run build"
                                } else {
                                    bat "npm ci || npm install"
                                    bat "npm run lint || ver > nul"
                                    bat "npm run build"
                                }
                            }
                        }
                    }
                    post {
                        success {
                            echo "--> [Frontend] Build Vite bundle thành công."
                        }
                        failure {
                            echo "--> [Frontend] Lỗi build Frontend!"
                        }
                    }
                }

                // 3. ML SERVICE: Python FastAPI
                stage('ML Service (FastAPI)') {
                    steps {
                        dir("${env.PROJECT_DIR}/MLService") {
                            script {
                                echo "--> [MLService] Kiểm tra cú pháp và cấu hình Python..."
                                if (isUnix()) {
                                    sh """
                                        python3 -m py_compile main.py || python -m py_compile main.py
                                    """
                                } else {
                                    bat "python -m py_compile main.py"
                                }
                            }
                        }
                    }
                    post {
                        success {
                            echo "--> [MLService] Kiểm tra cú pháp Python thành công."
                        }
                        failure {
                            echo "--> [MLService] Lỗi cú pháp trong MLService!"
                        }
                    }
                }
            }
        }

        // 4. BUILD DOCKER IMAGES
        stage('Build Docker Images') {
            when {
                expression { return params.BUILD_DOCKER_IMAGES }
            }
            steps {
                script {
                    echo "--> Đang đóng gói Docker Images cho toàn bộ hệ thống..."
                    def composeFile = "${env.PROJECT_DIR}/docker-compose.yml"
                    
                    if (isUnix()) {
                        sh "docker compose -f ${composeFile} build || docker-compose -f ${composeFile} build"
                    } else {
                        bat "docker compose -f ${composeFile} build"
                    }
                }
            }
        }

        // 5. PUSH DOCKER IMAGES (Tùy chọn)
        stage('Push Docker Images') {
            when {
                allOf {
                    expression { return params.PUSH_TO_REGISTRY }
                    expression { return params.BUILD_DOCKER_IMAGES }
                }
            }
            steps {
                script {
                    echo "--> Đang gắn tag và push Docker Images lên Registry: ${params.DOCKER_REGISTRY}..."
                    def registry = params.DOCKER_REGISTRY
                    def tag = env.IMAGE_TAG

                    /* 
                       Nếu cấu hình Docker Credentials trong Jenkins, có thể dùng:
                       withCredentials([usernamePassword(credentialsId: env.DOCKER_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                           sh "echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin"
                       }
                    */

                    def services = ['edumy-backend', 'edumy-frontend', 'edumy-mlservice']
                    for (service in services) {
                        def localImg = "${service}:latest"
                        def remoteImg = "${registry}/${service}:${tag}"
                        def remoteLatest = "${registry}/${service}:latest"
                        
                        if (isUnix()) {
                            sh "docker tag ${localImg} ${remoteImg} || true"
                            sh "docker tag ${localImg} ${remoteLatest} || true"
                            sh "docker push ${remoteImg} || true"
                            sh "docker push ${remoteLatest} || true"
                        } else {
                            bat "docker tag ${localImg} ${remoteImg} || ver > nul"
                            bat "docker tag ${localImg} ${remoteLatest} || ver > nul"
                            bat "docker push ${remoteImg} || ver > nul"
                            bat "docker push ${remoteLatest} || ver > nul"
                        }
                    }
                }
            }
        }

        // 6. DEPLOYMENT (DOCKER COMPOSE)
        stage('Deploy (Docker Compose)') {
            when {
                expression { return params.DEPLOY_APPLICATION }
            }
            steps {
                script {
                    echo "--> Đang khởi chạy hệ thống EduMy bằng Docker Compose..."
                    def composeFile = "${env.PROJECT_DIR}/docker-compose.yml"

                    if (isUnix()) {
                        sh "docker compose -f ${composeFile} down || docker-compose -f ${composeFile} down || true"
                        sh "docker compose -f ${composeFile} up -d || docker-compose -f ${composeFile} up -d"
                        sh "docker compose -f ${composeFile} ps || docker-compose -f ${composeFile} ps"
                    } else {
                        bat "docker compose -f ${composeFile} down"
                        bat "docker compose -f ${composeFile} up -d"
                        bat "docker compose -f ${composeFile} ps"
                    }

                    echo "==> EduMy đã được triển khai thành công!"
                    echo "    - Backend API : http://localhost:5000"
                    echo "    - ML Service  : http://localhost:8000"
                    echo "    - Frontend    : http://localhost:80"
                    echo "    - SQL Server  : localhost:1433"
                }
            }
        }
    }

    // Các hành động sau khi kết thúc pipeline
    post {
        always {
            script {
                echo "Pipeline hoàn tất."
            }
        }
        success {
            script {
                echo "=========================================================="
                echo " [THÀNH CÔNG] Pipeline #${env.BUILD_NUMBER} hoàn tất xuất sắc! "
                echo "=========================================================="
            }
        }
        failure {
            script {
                echo "=========================================================="
                echo " [THẤT BẠI] Pipeline #${env.BUILD_NUMBER} gặp lỗi! Vui lòng kiểm tra log."
                echo "=========================================================="
            }
        }
    }
}
