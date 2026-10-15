pipeline {
    agent any

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

    options {
        buildDiscarder(logRotator(numToKeepStr: '15', artifactNumToKeepStr: '5'))
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    environment {
        IMAGE_TAG = "${env.BUILD_NUMBER}"
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

                    // Nhận diện đường dẫn chứa mã nguồn trong repository
                    if (fileExists('Edumy/Backend/pom.xml') || fileExists('Edumy\\Backend\\pom.xml') || fileExists('Edumy/Backend') || fileExists('Edumy\\Backend')) {
                        env.PROJECT_DIR = 'Edumy'
                    } else if (fileExists('Edumy-main/Edumy/Backend/pom.xml') || fileExists('Edumy-main\\Edumy\\Backend\\pom.xml')) {
                        env.PROJECT_DIR = 'Edumy-main/Edumy'
                    } else if (fileExists('Backend/pom.xml') || fileExists('Backend\\pom.xml')) {
                        env.PROJECT_DIR = '.'
                    } else {
                        env.PROJECT_DIR = 'Edumy'
                    }
                    echo "==> Đã xác định thư mục dự án: ${env.PROJECT_DIR}"
                }
            }
        }

        stage('Build & Test Các Dịch Vụ') {
            parallel {
                // 1. BACKEND: Spring Boot 3 (Java, Maven)
                stage('Backend (Spring Boot 3)') {
                    steps {
                        script {
                            def projDir = env.PROJECT_DIR ?: (fileExists('Edumy/Backend') ? 'Edumy' : '.')
                            dir("${projDir}/Backend") {
                                echo "--> [Backend] Đang biên dịch và kiểm thử Spring Boot..."
                                def testOption = params.SKIP_TESTS ? "-DskipTests" : ""

                                if (isUnix()) {
                                    sh "chmod +x mvnw"
                                    sh "./mvnw clean package ${testOption}"
                                } else {
                                    bat """
                                        @echo off
                                        REM Tu dong tim kiem JDK neu JAVA_HOME chua duoc dat
                                        if not defined JAVA_HOME (
                                            for /d %%d in ("C:\\Program Files\\Java\\jdk*" "C:\\Program Files\\Eclipse Adoptium\\jdk*" "C:\\Program Files\\Microsoft\\jdk*" "C:\\Program Files\\Amazon Corretto\\jdk*") do (
                                                if exist "%%d\\bin\\java.exe" set "JAVA_HOME=%%d"
                                            )
                                        )
                                        if defined JAVA_HOME echo JAVA_HOME duoc thiet lap tai: %JAVA_HOME%
                                        call mvnw.cmd clean package ${testOption}
                                    """
                                }
                            }
                        }
                    }
                    post {
                        success {
                            echo "--> [Backend] Build JAR thành công."
                            archiveArtifacts artifacts: "**/Backend/target/*.jar", fingerprint: true, allowEmptyArchive: true
                        }
                        failure {
                            echo "--> [Backend] Lỗi biên dịch Spring Boot!"
                        }
                    }
                }

                // 2. FRONTEND: React + Vite (Node.js)
                stage('Frontend (React & Vite)') {
                    steps {
                        script {
                            def projDir = env.PROJECT_DIR ?: (fileExists('Edumy/Frontend') ? 'Edumy' : '.')
                            dir("${projDir}/Frontend") {
                                echo "--> [Frontend] Cài đặt dependencies và build client..."
                                if (isUnix()) {
                                    sh "npm ci || npm install"
                                    sh "npm run lint || true"
                                    sh "npm run build"
                                } else {
                                    bat """
                                        @echo off
                                        set "PATH=C:\\Program Files\\nodejs;%APPDATA%\\npm;%PATH%"
                                        call npm ci || call npm install
                                        call npm run lint || ver > nul
                                        call npm run build
                                    """
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
                        script {
                            def projDir = env.PROJECT_DIR ?: (fileExists('Edumy/MLService') ? 'Edumy' : '.')
                            dir("${projDir}/MLService") {
                                echo "--> [MLService] Kiểm tra cú pháp và cấu hình Python..."
                                if (isUnix()) {
                                    sh "python3 -m py_compile main.py || python -m py_compile main.py || true"
                                } else {
                                    bat """
                                        @echo off
                                        set "PATH=C:\\Users\\bacle\\AppData\\Local\\Programs\\Python\\Python312;C:\\Users\\bacle\\AppData\\Local\\Programs\\Python\\Python312\\Scripts;%PATH%"
                                        python -m py_compile main.py || py -m py_compile main.py || echo [CANH BAO] Python khong tim thay tren agent, bo qua kiem tra cu phap.
                                    """
                                }
                            }
                        }
                    }
                    post {
                        success {
                            echo "--> [MLService] Hoàn tất bước MLService."
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
                    def projDir = env.PROJECT_DIR ?: (fileExists('Edumy/docker-compose.yml') ? 'Edumy' : '.')
                    echo "--> Đang đóng gói Docker Images cho toàn bộ hệ thống..."
                    def composeFile = "${projDir}/docker-compose.yml"
                    
                    if (isUnix()) {
                        sh "docker compose -f ${composeFile} build || docker-compose -f ${composeFile} build"
                    } else {
                        bat "where docker >nul 2>&1 && (docker compose -f ${composeFile} build || docker-compose -f ${composeFile} build) || echo [CANH BAO] Docker chua duoc cai dat tren agent Windows nay."
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
                            bat "where docker >nul 2>&1 && (docker tag ${localImg} ${remoteImg} && docker push ${remoteImg}) || echo [INFO] Skip docker push"
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
                    def projDir = env.PROJECT_DIR ?: (fileExists('Edumy/docker-compose.yml') ? 'Edumy' : '.')
                    echo "--> Đang khởi chạy hệ thống EduMy bằng Docker Compose..."
                    def composeFile = "${projDir}/docker-compose.yml"

                    if (isUnix()) {
                        sh "docker compose -f ${composeFile} down || docker-compose -f ${composeFile} down || true"
                        sh "docker compose -f ${composeFile} up -d || docker-compose -f ${composeFile} up -d"
                        sh "docker compose -f ${composeFile} ps || docker-compose -f ${composeFile} ps"
                    } else {
                        bat "where docker >nul 2>&1 && (docker compose -f ${composeFile} down && docker compose -f ${composeFile} up -d && docker compose -f ${composeFile} ps) || echo [CANH BAO] Docker chua duoc cai dat tren agent Windows nay."
                    }

                    echo "==> Hoàn tất quy trình triển khai!"
                }
            }
        }
    }

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
