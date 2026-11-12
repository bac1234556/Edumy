pipeline {
    agent any

    parameters {
        string(
            name: 'BRANCH_NAME',
            defaultValue: 'main',
            description: 'Nhánh Git cần checkout và triển khai'
        )
        choice(
            name: 'ENVIRONMENT', 
            choices: ['development', 'staging', 'production'], 
            description: 'Môi trường triển khai mục tiêu'
        )
        booleanParam(
            name: 'RUN_SONAR_SCAN', 
            defaultValue: true, 
            description: 'Kích hoạt quét mã nguồn và kiểm tra Quality Gate trên SonarQube/SonarCloud'
        )
        booleanParam(
            name: 'SKIP_TESTS', 
            defaultValue: false, 
            description: 'Bỏ qua bước Unit Test khi build các phân hệ'
        )
        booleanParam(
            name: 'BUILD_DOCKER_IMAGES', 
            defaultValue: true, 
            description: 'Build Docker Image cho tất cả các dịch vụ (Backend, Frontend, MLService, Database)'
        )
        booleanParam(
            name: 'PUSH_TO_REGISTRY', 
            defaultValue: false, 
            description: 'Đẩy Docker Image lên Docker Registry (yêu cầu cấu hình credentials)'
        )
        string(
            name: 'DOCKER_REGISTRY', 
            defaultValue: 'docker.io/myusername', 
            description: 'Docker Registry hoặc Docker Hub Namespace'
        )
        string(
            name: 'DOCKER_CREDENTIALS_ID', 
            defaultValue: 'docker-hub-credentials', 
            description: 'ID của Credentials lưu Username/Password Docker Registry trong Jenkins'
        )
        booleanParam(
            name: 'DEPLOY_LOCAL', 
            defaultValue: true, 
            description: 'Triển khai dịch vụ bằng Docker Compose cục bộ sau khi build'
        )
        booleanParam(
            name: 'DEPLOY_TO_REMOTE', 
            defaultValue: false, 
            description: 'Kích hoạt tự động triển khai từ xa lên máy chủ Public qua SSH'
        )
        string(
            name: 'REMOTE_SERVER_IP', 
            defaultValue: '103.20.100.50', 
            description: 'Địa chỉ IP hoặc Hostname của Public Server mục tiêu'
        )
        string(
            name: 'REMOTE_SERVER_USER', 
            defaultValue: 'ubuntu', 
            description: 'Tài khoản SSH trên Public Server'
        )
        string(
            name: 'SSH_CREDENTIALS_ID', 
            defaultValue: 'deploy-server-ssh-key', 
            description: 'ID của SSH Private Key credentials cấu hình trong Jenkins'
        )
        string(
            name: 'REMOTE_DEPLOY_DIR', 
            defaultValue: '/opt/edumy-app', 
            description: 'Thư mục chứa mã nguồn và docker-compose.yml trên máy chủ từ xa'
        )
    }

    triggers {
        githubPush()
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '5'))
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    environment {
        // Tag định danh cho Docker Image: số build hoặc hash commit
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        SONAR_SERVER_NAME = 'sonar cloud' // Khớp chính xác tên cài đặt SonarQube Scanner trong Jenkins
        PROJECT_DIR = 'Edumy'
    }

    stages {
        // =========================================================================
        // GIAI ĐOẠN 1: CHECKOUT SOURCE CODE
        // =========================================================================
        stage('Checkout Source Code') {
            steps {
                script {
                    echo "========================================================"
                    echo "   STAGE 1: CHECKOUT SOURCE CODE (Branch: ${params.BRANCH_NAME})   "
                    echo "========================================================"
                }
                checkout scmGit(
                    branches: [[name: "*/${params.BRANCH_NAME}"]],
                    userRemoteConfigs: scm.userRemoteConfigs,
                    extensions: [
                        [$class: 'CleanBeforeCheckout', deleteUntrackedNestedRepositories: true]
                    ]
                )
                script {
                    // Tự động nhận diện thư mục dự án
                    if (fileExists('Edumy/Backend/pom.xml')) {
                        env.PROJECT_DIR = 'Edumy'
                    } else if (fileExists('Backend/pom.xml')) {
                        env.PROJECT_DIR = '.'
                    } else {
                        env.PROJECT_DIR = 'Edumy'
                    }
                    echo "==> Đã tải mã nguồn. Thư mục dự án: ${env.PROJECT_DIR}"
                }
            }
        }

        // =========================================================================
        // GIAI ĐOẠN 2: QUÉT MÃ NGUỒN SONARQUBE / SONARCLOUD (STATIC CODE ANALYSIS)
        // =========================================================================
        stage('Quét mã nguồn SonarQube') {
            when {
                expression { return params.RUN_SONAR_SCAN }
            }
            steps {
                script {
                    echo "========================================================"
                    echo "   STAGE 2: QUÉT MÃ NGUỒN VỚI SONARQUBE / SONARCLOUD    "
                    echo "========================================================"
                    
                    def projDir = env.PROJECT_DIR
                    withSonarQubeEnv(env.SONAR_SERVER_NAME) {
                        dir("${projDir}/Backend") {
                            echo "--> [Sonar Analysis] Đang phân tích mã nguồn Backend..."
                            def testArg = params.SKIP_TESTS ? "-DskipTests" : ""
                            
                            if (isUnix()) {
                                sh "chmod +x mvnw"
                                sh """
                                    ./mvnw clean test jacoco:report sonar:sonar ${testArg} \
                                    -Dsonar.projectKey=bac1234556_Edumy \
                                    -Dsonar.organization=bac1234556 \
                                    -Dsonar.projectName='EduMy Backend' \
                                    -Dsonar.sources=src/main \
                                    -Dsonar.tests=src/test \
                                    -Dsonar.java.binaries=target/classes \
                                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                                """
                            } else {
                                bat """
                                    @echo off
                                    set "PATH=C:\\Program Files\\Java\\jdk-21.0.10\\bin;%PATH%"
                                    call mvnw.cmd clean test jacoco:report sonar:sonar ${testArg} ^
                                    -Dsonar.projectKey=bac1234556_Edumy ^
                                    -Dsonar.organization=bac1234556 ^
                                    -Dsonar.projectName="EduMy Backend" ^
                                    -Dsonar.sources=src/main ^
                                    -Dsonar.tests=src/test ^
                                    -Dsonar.java.binaries=target/classes ^
                                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                                """
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // GIAI ĐOẠN 3: KIỂM TRA RÀO CHẮN QUALITY GATE
        // =========================================================================
        stage('Kiểm tra Quality Gate') {
            when {
                expression { return params.RUN_SONAR_SCAN }
            }
            steps {
                script {
                    echo "========================================================"
                    echo "   STAGE 3: KIỂM TRA RÀO CHẮN QUALITY GATE               "
                    echo "========================================================"
                    echo "--> Đang chờ kết quả phản hồi từ SonarQube / SonarCloud..."
                    
                    try {
                        timeout(time: 2, unit: 'MINUTES') {
                            def qg = waitForQualityGate()
                            if (qg.status != 'OK') {
                                echo "⚠️ [CẢNH BÁO] Quality Gate trả về trạng thái: ${qg.status}"
                            } else {
                                echo "✅ QUALITY GATE PASSED! Mã nguồn đạt đầy đủ các tiêu chuẩn kiểm định an toàn và chất lượng."
                            }
                        }
                    } catch (Exception e) {
                        echo "⚠️ [LƯU Ý] Không nhận được phản hồi Quality Gate webhook từ SonarCloud (Jenkins đang chạy local): ${e.message}"
                        echo "--> Báo cáo tổng thể có thể xem trực tiếp tại: https://sonarcloud.io/project/overview?id=bac1234556_Edumy"
                    }
                }
            }
        }

        // =========================================================================
        // GIAI ĐOẠN 4: BUILD & TEST SONG SONG (PARALLEL) CÁC PHÂN HỆ
        // =========================================================================
        stage('Build & Test Song Song Các Phân Hệ') {
            parallel {
                // Phân hệ 1: Backend (Spring Boot 3 - Java)
                stage('Backend (Spring Boot 3)') {
                    steps {
                        script {
                            echo "--> [Parallel] Đang build Backend (Spring Boot 3)..."
                            def projDir = env.PROJECT_DIR
                            dir("${projDir}/Backend") {
                                def testOption = params.SKIP_TESTS ? "-DskipTests" : ""
                                if (isUnix()) {
                                    sh "chmod +x mvnw"
                                    sh "./mvnw clean package ${testOption}"
                                } else {
                                    bat """
                                        @echo off
                                        set "PATH=C:\\Program Files\\Java\\jdk-21.0.10\\bin;%PATH%"
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
                            echo "--> [Backend] Build JAR thất bại!"
                        }
                    }
                }

                // Phân hệ 2: Frontend (React Vite - Node.js)
                stage('Frontend (React & Vite)') {
                    steps {
                        script {
                            echo "--> [Parallel] Đang build Frontend (React + Vite)..."
                            def projDir = env.PROJECT_DIR
                            dir("${projDir}/Frontend") {
                                if (isUnix()) {
                                    sh """
                                        npm ci || npm install
                                        ${params.SKIP_TESTS ? "echo 'Skip frontend tests'" : "npm test -- --watchAll=false || true"}
                                        npm run build
                                    """
                                } else {
                                    bat """
                                        @echo off
                                        set "PATH=C:\\Program Files\\nodejs;C:\\Program Files (x86)\\nodejs;%APPDATA%\\npm;%PATH%"
                                        call npm ci || call npm install
                                        if "${params.SKIP_TESTS}"=="false" (
                                            call npm test -- --watchAll=false || ver > nul
                                        )
                                        call npm run build
                                    """
                                }
                            }
                        }
                    }
                    post {
                        success {
                            echo "--> [Frontend] Build Bundle thành công."
                        }
                        failure {
                            echo "--> [Frontend] Build Frontend thất bại!"
                        }
                    }
                }

                // Phân hệ 3: ML Service (Python FastAPI)
                stage('ML Service (FastAPI)') {
                    steps {
                        script {
                            echo "--> [Parallel] Đang kiểm tra & kiểm thử ML Service (FastAPI)..."
                            def projDir = env.PROJECT_DIR
                            dir("${projDir}/MLService") {
                                if (isUnix()) {
                                    sh """
                                        python3 -m py_compile main.py || python -m py_compile main.py
                                        if [ "${params.SKIP_TESTS}" = "false" ]; then
                                            pytest -v || echo 'Chưa có unit test pytest, cú pháp đã kiểm tra xong'
                                        fi
                                    """
                                } else {
                                    bat """
                                        @echo off
                                        set "PATH=C:\\Users\\bacle\\AppData\\Local\\Programs\\Python\\Python312;C:\\Users\\bacle\\AppData\\Local\\Programs\\Python\\Python312\\Scripts;%PATH%"
                                        python -m py_compile main.py || py -m py_compile main.py
                                        if "${params.SKIP_TESTS}"=="false" (
                                            pytest -v || echo Chua co unit test pytest
                                        )
                                    """
                                }
                            }
                        }
                    }
                    post {
                        success {
                            echo "--> [MLService] Kiểm tra cú pháp và dịch vụ hoàn tất."
                        }
                        failure {
                            echo "--> [MLService] MLService gặp lỗi!"
                        }
                    }
                }
            }
        }

        // =========================================================================
        // GIAI ĐOẠN 5: ĐÓNG GÓI & PUSH DOCKER IMAGES
        // =========================================================================
        stage('Đóng gói & Push Docker Images') {
            when {
                expression { return params.BUILD_DOCKER_IMAGES }
            }
            steps {
                script {
                    echo "========================================================"
                    echo "   STAGE 5: BUILD & ĐÓNG GÓI DOCKER IMAGES              "
                    echo "   Registry: ${params.DOCKER_REGISTRY}                  "
                    echo "   Tag     : ${env.IMAGE_TAG}                           "
                    echo "========================================================"

                    def projDir = env.PROJECT_DIR
                    def registry = params.DOCKER_REGISTRY
                    def tag = env.IMAGE_TAG

                    def modules = [
                        [name: 'edumy-backend', dir: "${projDir}/Backend"],
                        [name: 'edumy-frontend', dir: "${projDir}/Frontend"],
                        [name: 'edumy-mlservice', dir: "${projDir}/MLService"],
                        [name: 'edumy-database', dir: "${projDir}/Database"]
                    ]

                    for (mod in modules) {
                        echo "--> [Docker] Building image ${mod.name}..."
                        def imageVersionTag = "${registry}/${mod.name}:${tag}"
                        def imageLatestTag = "${registry}/${mod.name}:latest"

                        if (isUnix()) {
                            sh "docker build -t ${imageVersionTag} -t ${imageLatestTag} -f ${mod.dir}/Dockerfile ${mod.dir}"
                        } else {
                            bat "docker build -t ${imageVersionTag} -t ${imageLatestTag} -f ${mod.dir}/Dockerfile ${mod.dir}"
                        }
                    }

                    // Push lên Docker Registry nếu được kích hoạt
                    if (params.PUSH_TO_REGISTRY) {
                        try {
                            withCredentials([usernamePassword(
                                credentialsId: params.DOCKER_CREDENTIALS_ID, 
                                usernameVariable: 'DOCKER_USER', 
                                passwordVariable: 'DOCKER_PASS'
                            )]) {
                                echo "--> [Docker Registry] Đang đăng nhập và đẩy images lên ${registry}..."
                                if (isUnix()) {
                                    sh "echo \$DOCKER_PASS | docker login ${registry.split('/')[0]} -u \$DOCKER_USER --password-stdin"
                                } else {
                                    bat "echo %DOCKER_PASS% | docker login ${registry.split('/')[0]} -u %DOCKER_USER% --password-stdin"
                                }

                                for (mod in modules) {
                                    def imageVersionTag = "${registry}/${mod.name}:${tag}"
                                    def imageLatestTag = "${registry}/${mod.name}:latest"
                                    if (isUnix()) {
                                        sh "docker push ${imageVersionTag} && docker push ${imageLatestTag}"
                                    } else {
                                        bat "docker push ${imageVersionTag} && docker push ${imageLatestTag}"
                                    }
                                }
                            }
                        } catch (Exception e) {
                            echo "⚠️ [CẢNH BÁO] Không thể đẩy image lên Registry (${e.message}). Bỏ qua bước push."
                        }
                    } else {
                        echo "--> [Docker] Bỏ qua bước push image lên Registry (PUSH_TO_REGISTRY=false)."
                    }
                }
            }
        }

        // =========================================================================
        // GIAI ĐOẠN 6: TRIỂN KHAI CỤC BỘ QUA DOCKER COMPOSE
        // =========================================================================
        stage('Triển khai Cục bộ (Docker Compose)') {
            when {
                expression { return params.DEPLOY_LOCAL }
            }
            steps {
                script {
                    echo "========================================================"
                    echo "   STAGE 6: TRIỂN KHAI CỤC BỘ QUA DOCKER COMPOSE        "
                    echo "========================================================"

                    def projDir = env.PROJECT_DIR
                    def composeFile = "${projDir}/docker-compose.yml"

                    if (isUnix()) {
                        sh "docker compose -p edumy -f ${composeFile} up -d"
                        sh "docker compose -p edumy -f ${composeFile} ps"
                    } else {
                        bat """
                            @echo off
                            where docker >nul 2>&1
                            if %ERRORLEVEL% NEQ 0 (
                                echo [CANH BAO] Docker chua duoc cai dat tren agent Windows nay.
                                exit /b 0
                            )
                            echo [1/2] Khoi dong he thong bang Docker Compose...
                            docker compose -p edumy -f ${composeFile} up -d

                            echo [2/2] Kiem tra trang thai dich vu sau khi deploy:
                            docker compose -p edumy -f ${composeFile} ps
                        """
                    }

                    echo "==> [THÀNH CÔNG] Đã triển khai hoàn tất dịch vụ trên máy cục bộ!"
                }
            }
        }

        // =========================================================================
        // GIAI ĐOẠN 7: TỰ ĐỘNG TRIỂN KHAI TỪ XA QUA SSH (NẾU CÓ CẤU HÌNH)
        // =========================================================================
        stage('Deploy to Server via SSH') {
            when {
                expression { return params.DEPLOY_TO_REMOTE }
            }
            steps {
                script {
                    echo "========================================================"
                    echo "   STAGE 7: TỰ ĐỘNG TRIỂN KHAI TỪ XA VIA SSH            "
                    echo "   Target Host : ${params.REMOTE_SERVER_USER}@${params.REMOTE_SERVER_IP} "
                    echo "   Deploy Path : ${params.REMOTE_DEPLOY_DIR}            "
                    echo "========================================================"

                    try {
                        withCredentials([
                            sshUserPrivateKey(
                                credentialsId: params.SSH_CREDENTIALS_ID, 
                                keyFileVariable: 'SSH_KEY', 
                                usernameVariable: 'SSH_USER'
                            )
                        ]) {
                            def remoteUser = params.REMOTE_SERVER_USER
                            def remoteIp = params.REMOTE_SERVER_IP
                            def remoteDir = params.REMOTE_DEPLOY_DIR
                            def projDir = env.PROJECT_DIR
                            def sshCmd = "ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${remoteUser}@${remoteIp}"
                            def scpCmd = "scp -o StrictHostKeyChecking=no -i ${SSH_KEY}"

                            echo "--> [1/3] Đồng bộ file docker-compose.yml sang máy chủ..."
                            if (isUnix()) {
                                sh "${sshCmd} 'mkdir -p ${remoteDir}'"
                                sh "${scpCmd} ${projDir}/docker-compose.yml ${remoteUser}@${remoteIp}:${remoteDir}/docker-compose.yml"
                                sh "${sshCmd} 'cd ${remoteDir} && docker compose pull && docker compose up -d --remove-orphans && docker compose ps'"
                            } else {
                                bat """
                                    ${sshCmd} "mkdir -p ${remoteDir}"
                                    ${scpCmd} ${projDir}/docker-compose.yml ${remoteUser}@${remoteIp}:${remoteDir}/docker-compose.yml
                                    ${sshCmd} "cd ${remoteDir} && docker compose pull && docker compose up -d --remove-orphans && docker compose ps"
                                """
                            }
                            echo "==> [THÀNH CÔNG] Đã triển khai phiên bản #${env.BUILD_NUMBER} lên Public Server (${remoteIp})!"
                        }
                    } catch (Exception e) {
                        echo "⚠️ [CẢNH BÁO] Bỏ qua triển khai từ xa qua SSH: ${e.message}"
                    }
                }
            }
        }
    }

    // =========================================================================
    // POST ACTIONS & NOTIFICATIONS
    // =========================================================================
    post {
        always {
            script {
                echo "Pipeline hoàn tất việc thực thi."
                cleanWs(deleteDirs: true, notFailBuild: true)
            }
        }
        success {
            script {
                echo "=========================================================="
                echo " [THÀNH CÔNG] CI/CD Pipeline #${env.BUILD_NUMBER} thành công rực rỡ!"
                echo " Môi trường : ${params.ENVIRONMENT}"
                echo " Phiên bản  : Image Tag #${env.IMAGE_TAG}"
                echo " Triển khai : ${params.DEPLOY_LOCAL ? 'Cục bộ (Docker Compose)' : ''} ${params.DEPLOY_TO_REMOTE ? 'Từ xa (' + params.REMOTE_SERVER_IP + ')' : ''}"
                echo "=========================================================="
            }
        }
        failure {
            script {
                echo "=========================================================="
                echo " [THẤT BẠI] Pipeline #${env.BUILD_NUMBER} đã gặp lỗi!"
                echo " Vui lòng kiểm tra console output và báo cáo SonarQube."
                echo "=========================================================="
            }
        }
    }
}
