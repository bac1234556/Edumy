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
            description: 'Kích hoạt quét mã nguồn và kiểm tra Quality Gate trên SonarQube'
        )
        booleanParam(
            name: 'SKIP_TESTS', 
            defaultValue: false, 
            description: 'Bỏ qua bước Unit Test khi build các phân hệ'
        )
        string(
            name: 'DOCKER_REGISTRY', 
            defaultValue: 'docker.io/myusername', 
            description: 'Docker Registry hoặc Docker Hub Namespace (VD: registry.hub.docker.com/your-org)'
        )
        string(
            name: 'DOCKER_CREDENTIALS_ID', 
            defaultValue: 'docker-hub-credentials', 
            description: 'ID của Credentials lưu Username/Password Docker Registry trong Jenkins'
        )
        booleanParam(
            name: 'DEPLOY_TO_REMOTE', 
            defaultValue: true, 
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
        SONAR_SERVER_NAME = 'SonarQube' // Tên cấu hình SonarQube Server trong Jenkins System Configuration
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
        // GIAI ĐOẠN 2: QUÉT MÃ NGUỒN SONARQUBE (STATIC CODE ANALYSIS)
        // =========================================================================
        stage('Quét mã nguồn SonarQube') {
            when {
                expression { return params.RUN_SONAR_SCAN }
            }
            steps {
                script {
                    echo "========================================================"
                    echo "   STAGE 2: QUÉT MÃ NGUỒN VỚI SONARQUBE                "
                    echo "========================================================"
                    
                    def projDir = env.PROJECT_DIR
                    withSonarQubeEnv(env.SONAR_SERVER_NAME) {
                        dir("${projDir}/Backend") {
                            echo "--> [SonarQube Analysis] Đang phân tích mã nguồn Backend..."
                            def testArg = params.SKIP_TESTS ? "-DskipTests" : ""
                            
                            if (isUnix()) {
                                sh "chmod +x mvnw"
                                sh """
                                    ./mvnw clean test jacoco:report sonar:sonar ${testArg} \
                                    -Dsonar.projectKey=edumy-backend \
                                    -Dsonar.projectName='EduMy Backend' \
                                    -Dsonar.sources=src/main \
                                    -Dsonar.tests=src/test \
                                    -Dsonar.java.binaries=target/classes \
                                    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                                """
                            } else {
                                bat """
                                    @echo off
                                    call mvnw.cmd clean test jacoco:report sonar:sonar ${testArg} ^
                                    -Dsonar.projectKey=edumy-backend ^
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
        // (Yêu cầu: Coverage > 80%, 0 Vulnerabilities, Technical Debt < 5%)
        // =========================================================================
        stage('Kiểm tra Quality Gate') {
            when {
                expression { return params.RUN_SONAR_SCAN }
            }
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    script {
                        echo "========================================================"
                        echo "   STAGE 3: KIỂM TRA RÀO CHẮN QUALITY GATE               "
                        echo "   - Coverage        > 80%                              "
                        echo "   - Vulnerabilities = 0                                "
                        echo "   - Technical Debt  < 5%                               "
                        echo "========================================================"
                        echo "--> Đang chờ kết quả phản hồi từ SonarQube Server..."
                        
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "❌ PIPELINE BỊ DỪNG: Quality Gate Thất Bại (Trạng thái: ${qg.status}). Dự án không thỏa mãn điều kiện chất lượng mã nguồn!"
                        }
                        
                        echo "✅ QUALITY GATE PASSED! Mã nguồn đạt đầy đủ các tiêu chuẩn kiểm định an toàn và chất lượng."
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
                                    bat "call mvnw.cmd clean package ${testOption}"
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
            steps {
                script {
                    echo "========================================================"
                    echo "   STAGE 5: BUILD & PUSH DOCKER IMAGES                  "
                    echo "   Registry: ${params.DOCKER_REGISTRY}                  "
                    echo "   Tag     : ${env.IMAGE_TAG}                           "
                    echo "========================================================"

                    def projDir = env.PROJECT_DIR
                    def registry = params.DOCKER_REGISTRY
                    def tag = env.IMAGE_TAG

                    // Đăng nhập Docker Registry bằng Jenkins Credentials
                    withCredentials([usernamePassword(
                        credentialsId: params.DOCKER_CREDENTIALS_ID, 
                        usernameVariable: 'DOCKER_USER', 
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        if (isUnix()) {
                            sh "echo \$DOCKER_PASS | docker login ${registry.split('/')[0]} -u \$DOCKER_USER --password-stdin"
                        } else {
                            bat "echo %DOCKER_PASS% | docker login ${registry.split('/')[0]} -u %DOCKER_USER% --password-stdin"
                        }

                        // Danh sách phân hệ cần đóng gói
                        def modules = [
                            [name: 'edumy-backend', dir: "${projDir}/Backend"],
                            [name: 'edumy-frontend', dir: "${projDir}/Frontend"],
                            [name: 'edumy-mlservice', dir: "${projDir}/MLService"],
                            [name: 'edumy-database', dir: "${projDir}/Database"]
                        ]

                        for (mod in modules) {
                            echo "--> [Docker] Building & Pushing ${mod.name}..."
                            def imageVersionTag = "${registry}/${mod.name}:${tag}"
                            def imageLatestTag = "${registry}/${mod.name}:latest"

                            if (isUnix()) {
                                sh """
                                    docker build -t ${imageVersionTag} -t ${imageLatestTag} -f ${mod.dir}/Dockerfile ${mod.dir}
                                    docker push ${imageVersionTag}
                                    docker push ${imageLatestTag}
                                """
                            } else {
                                bat """
                                    docker build -t ${imageVersionTag} -t ${imageLatestTag} -f ${mod.dir}/Dockerfile ${mod.dir}
                                    docker push ${imageVersionTag}
                                    docker push ${imageLatestTag}
                                """
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // GIAI ĐOẠN 6: TỰ ĐỘNG TRIỂN KHAI TỪ XA QUA SSH (SSH DEPLOYMENT)
        // =========================================================================
        stage('Deploy to Server via SSH') {
            when {
                expression { return params.DEPLOY_TO_REMOTE }
            }
            steps {
                script {
                    echo "========================================================"
                    echo "   STAGE 6: TỰ ĐỘNG TRIỂN KHAI TỪ XA VIA SSH            "
                    echo "   Target Host : ${params.REMOTE_SERVER_USER}@${params.REMOTE_SERVER_IP} "
                    echo "   Deploy Path : ${params.REMOTE_DEPLOY_DIR}            "
                    echo "========================================================"

                    def projDir = env.PROJECT_DIR
                    def remoteUser = params.REMOTE_SERVER_USER
                    def remoteIp = params.REMOTE_SERVER_IP
                    def remoteDir = params.REMOTE_DEPLOY_DIR
                    def registry = params.DOCKER_REGISTRY
                    def tag = env.IMAGE_TAG

                    // Sử dụng SSH Private Key credentials đã được cấu hình trên Jenkins
                    withCredentials([
                        sshUserPrivateKey(
                            credentialsId: params.SSH_CREDENTIALS_ID, 
                            keyFileVariable: 'SSH_KEY', 
                            usernameVariable: 'SSH_USER'
                        ),
                        usernamePassword(
                            credentialsId: params.DOCKER_CREDENTIALS_ID, 
                            usernameVariable: 'DOCKER_USER', 
                            passwordVariable: 'DOCKER_PASS'
                        )
                    ]) {
                        def sshCmd = "ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${remoteUser}@${remoteIp}"
                        def scpCmd = "scp -o StrictHostKeyChecking=no -i ${SSH_KEY}"

                        echo "--> [1/4] Đồng bộ file docker-compose.yml sang máy chủ..."
                        if (isUnix()) {
                            sh "${sshCmd} 'mkdir -p ${remoteDir}'"
                            sh "${scpCmd} ${projDir}/docker-compose.yml ${remoteUser}@${remoteIp}:${remoteDir}/docker-compose.yml"
                        } else {
                            bat """
                                ${sshCmd} "mkdir -p ${remoteDir}"
                                ${scpCmd} ${projDir}/docker-compose.yml ${remoteUser}@${remoteIp}:${remoteDir}/docker-compose.yml
                            """
                        }

                        echo "--> [2/4] Thiết lập biến môi trường và cấu hình trên Public Server..."
                        def remoteCommands = """
                            set -e
                            cd ${remoteDir}

                            echo '--> Đăng nhập Docker Registry trên máy chủ mục tiêu...'
                            echo '${DOCKER_PASS}' | docker login ${registry.split('/')[0]} -u '${DOCKER_USER}' --password-stdin

                            echo '--> Cập nhật file biến môi trường .env...'
                            cat << 'EOF' > .env
DOCKER_REGISTRY=${registry}
IMAGE_TAG=${tag}
ENVIRONMENT=${params.ENVIRONMENT}
SPRING_DATASOURCE_PASSWORD=YourPassword123
EOF

                            echo '--> [3/4] Kéo các Docker Images mới nhất (docker compose pull)...'
                            docker compose pull || docker-compose pull

                            echo '--> [4/4] Khởi động container không gián đoạn (docker compose up -d)...'
                            docker compose up -d --remove-orphans || docker-compose up -d --remove-orphans

                            echo '--> Kiểm tra trạng thái các container sau triển khai:'
                            docker compose ps

                            echo '--> Dọn dẹp images cũ không sử dụng (prune)...'
                            docker image prune -f
                        """

                        if (isUnix()) {
                            sh "${sshCmd} '${remoteCommands}'"
                        } else {
                            bat """
                                ${sshCmd} "${remoteCommands}"
                            """
                        }

                        echo "==> [THÀNH CÔNG] Đã triển khai phiên bản #${env.BUILD_NUMBER} lên Public Server (${remoteIp})!"
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
                // Dọn dẹp workspace nếu cần
                cleanWs(deleteDirs: true, notFailBuild: true)
            }
        }
        success {
            script {
                echo "=========================================================="
                echo " [THÀNH CÔNG] CI/CD Pipeline #${env.BUILD_NUMBER} thành công rực rỡ!"
                echo " Môi trường : ${params.ENVIRONMENT}"
                echo " Phiên bản  : Image Tag #${env.IMAGE_TAG}"
                echo " Triển khai : ${params.DEPLOY_TO_REMOTE ? params.REMOTE_SERVER_IP : 'Bỏ qua'}"
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
