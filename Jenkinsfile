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
            description: 'Build Docker Image cho các dịch vụ (Backend, Frontend)'
        )
        booleanParam(
            name: 'PUSH_TO_REGISTRY', 
            defaultValue: true,
            description: 'Đẩy Docker Image lên Docker Registry'
        )
        string(
            name: 'DOCKER_REGISTRY', 
            defaultValue: 'docker.io/lynkenion/edumy',
            description: 'Docker Hub repo: docker.io/lynkenion/edumy'
        )
        string(
            name: 'DOCKER_CREDENTIALS_ID',
            defaultValue: 'docker-hub-credentials',
            description: 'Jenkins Username/Password credential dùng để push image'
        )
        choice(
            name: 'DEPLOY_TARGET',
            choices: ['kubernetes', 'docker-compose'],
            description: 'Nền tảng triển khai'
        )
        string(
            name: 'KUBECONFIG_CREDENTIAL_ID',
            defaultValue: 'kubeconfig',
            description: 'Jenkins Secret file credential chứa kubeconfig'
        )
        booleanParam(
            name: 'DEPLOY_APPLICATION', 
            defaultValue: true, 
            description: 'Triển khai ứng dụng lên nền tảng đã chọn'
        )
    }

    triggers {
        githubPush()
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
                    echo "Deploy Target  : ${params.DEPLOY_TARGET}"
                    echo "========================================================"

                    if (!(params.DOCKER_REGISTRY ==~ /[a-zA-Z0-9][a-zA-Z0-9._:-]*(\/[a-zA-Z0-9][a-zA-Z0-9._-]*)+/)) {
                        error("DOCKER_REGISTRY không hợp lệ. Dùng image prefix như docker.io/lynkenion.")
                    }

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
                                        set "FOUND_JDK="
                                        for /d %%d in ("C:\\Program Files\\Java\\jdk-17*" "C:\\Program Files\\Eclipse Adoptium\\jdk-17*" "C:\\Program Files\\Microsoft\\jdk-17*" "C:\\Program Files\\Java\\jdk-21*" "C:\\Program Files\\Eclipse Adoptium\\jdk-21*") do (
                                            if exist "%%d\\bin\\java.exe" if not defined FOUND_JDK set "FOUND_JDK=%%d"
                                        )
                                        if not defined FOUND_JDK (
                                            for /d %%d in ("C:\\Program Files\\Java\\jdk*" "C:\\Program Files\\Eclipse Adoptium\\jdk*" "C:\\Program Files\\Microsoft\\jdk*" "C:\\Program Files\\Amazon Corretto\\jdk*") do (
                                                if exist "%%d\\bin\\java.exe" if not defined FOUND_JDK set "FOUND_JDK=%%d"
                                            )
                                        )
                                        if defined FOUND_JDK (
                                            set "JAVA_HOME=%FOUND_JDK%"
                                            echo [INFO] Thiet lap JAVA_HOME: %FOUND_JDK%
                                        )
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
                                    sh "which npm && (npm ci || npm install) && (npm run lint || true) && npm run build || echo '[CANH BAO] npm khong tim thay tren agent'"
                                } else {
                                    bat """
                                        @echo off
                                        set "PATH=C:\\Program Files\\nodejs;C:\\Program Files (x86)\\nodejs;%APPDATA%\\npm;%PATH%"
                                        where npm >nul 2>&1
                                        if %ERRORLEVEL% NEQ 0 (
                                            echo [CANH BAO] npm khong tim thay tren agent, bo qua build client frontend.
                                            exit /b 0
                                        ) else (
                                            call npm ci || call npm install
                                            call npm run lint || ver > nul
                                            call npm run build
                                        )
                                    """
                                }
                            }
                        }
                    }
                    post {
                        success {
                            echo "--> [Frontend] Hoàn tất bước Frontend."
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
                    echo "--> Đang đóng gói Docker Images cho Backend và Frontend (DB chỉ build nội bộ, không push)..."
                    dir("${projDir}") {
                        def registry = params.DOCKER_REGISTRY
                        def tag = env.IMAGE_TAG
                        if (isUnix()) {
                            sh """
                                docker build -t edumy-database:latest -f Database/Dockerfile Database &&
                                docker build -t ${registry}:backend-${tag} -f Backend/Dockerfile Backend &&
                                docker build -t ${registry}:frontend-${tag} -f Frontend/Dockerfile Frontend
                            """
                        } else {
                            bat """
                                docker build -t edumy-database:latest -f Database/Dockerfile Database
                                if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
                                docker build -t ${registry}:backend-${tag} -f Backend/Dockerfile Backend
                                if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
                                docker build -t ${registry}:frontend-${tag} -f Frontend/Dockerfile Frontend
                            """
                        }
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
                    echo "--> Đang gắn tag và push Docker Images lên Registry repo: ${params.DOCKER_REGISTRY}..."
                    def registry = params.DOCKER_REGISTRY
                    def tag = env.IMAGE_TAG
                    def dockerServer = (registry.startsWith('docker.io/') || registry.startsWith('lynkenion/')) ? 'https://index.docker.io/v1/' : registry.tokenize('/')[0]

                    withCredentials([usernamePassword(
                        credentialsId: params.DOCKER_CREDENTIALS_ID,
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        withEnv(["DOCKER_SERVER=${dockerServer}"]) {
                            try {
                                if (isUnix()) {
                                    sh 'echo "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USER" --password-stdin "$DOCKER_SERVER"'
                                } else {
                                    bat 'echo %DOCKER_PASSWORD%| docker login --username "%DOCKER_USER%" --password-stdin "%DOCKER_SERVER%"'
                                }

                                def components = ['backend', 'frontend']
                                for (comp in components) {
                                    def remoteImg = "${registry}:${comp}-${tag}"
                                    def remoteLatest = "${registry}:${comp}-latest"
                                    echo "--> Đang push ${comp} (${remoteImg} & ${remoteLatest})..."
                                    if (isUnix()) {
                                        sh "docker tag ${remoteImg} ${remoteLatest} && docker push ${remoteImg} && docker push ${remoteLatest}"
                                    } else {
                                        bat "docker tag ${remoteImg} ${remoteLatest} && docker push ${remoteImg} && docker push ${remoteLatest}"
                                    }
                                }
                            } finally {
                                if (isUnix()) {
                                    sh 'docker logout "$DOCKER_SERVER" || true'
                                } else {
                                    bat 'docker logout "%DOCKER_SERVER%" >nul 2>&1 || ver > nul'
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. DEPLOYMENT (DOCKER COMPOSE)
        stage('Deploy (Docker Compose)') {
            when {
                expression { return params.DEPLOY_APPLICATION && params.DEPLOY_TARGET == 'docker-compose' }
            }
            steps {
                script {
                    if (!params.BUILD_DOCKER_IMAGES) {
                        error('Triển khai Docker Compose cần BUILD_DOCKER_IMAGES bật để tạo image của build hiện tại.')
                    }
                    def projDir = env.PROJECT_DIR ?: (fileExists('Edumy/docker-compose.yml') ? 'Edumy' : '.')
                    echo "--> Dang khoi chay he thong EduMy bang Docker Compose..."
                    def composeFile = "${projDir}/docker-compose.yml"

                    withEnv(["DOCKER_REGISTRY=${params.DOCKER_REGISTRY}", "IMAGE_TAG=${env.IMAGE_TAG}"]) {
                        if (isUnix()) {
                            withEnv(["COMPOSE_FILE=${composeFile}"]) {
                                sh '''
                                    set -eu
                                    if docker compose version >/dev/null 2>&1; then
                                        COMPOSE="docker compose"
                                    elif command -v docker-compose >/dev/null 2>&1; then
                                        COMPOSE="docker-compose"
                                    else
                                        echo "Docker Compose is not installed on this Jenkins agent." >&2
                                        exit 1
                                    fi
                                    docker rm -f edumy_sqlserver edumy_backend edumy_frontend || true
                                    $COMPOSE -p edumy -f "$COMPOSE_FILE" down --remove-orphans || true
                                    $COMPOSE -p edumy -f "$COMPOSE_FILE" up -d
                                    $COMPOSE -p edumy -f "$COMPOSE_FILE" ps
                                '''
                            }
                        } else {
                            bat """
                                @echo off
                                where docker >nul 2>&1
                                if %ERRORLEVEL% NEQ 0 (
                                    echo [CANH BAO] Docker chua duoc cai dat tren agent Windows nay.
                                    exit /b 1
                                )
                                docker compose version >nul 2>&1
                                if not errorlevel 1 goto useComposeV2
                                where docker-compose >nul 2>&1
                                if errorlevel 1 goto composeUnavailable
                                set "COMPOSE_COMMAND=docker-compose"
                                goto composeReady
                                :useComposeV2
                                set "COMPOSE_COMMAND=docker compose"
                                :composeReady
                                echo [1/3] Don dep container cu tranh xung dot ten hoac port...
                                docker rm -f edumy_sqlserver edumy_backend edumy_frontend 2>nul
                                %COMPOSE_COMMAND% -p edumy -f ${composeFile} down --remove-orphans
                                echo [2/3] Khoi dong he thong bang Docker Compose...
                                %COMPOSE_COMMAND% -p edumy -f ${composeFile} up -d
                                if errorlevel 1 exit /b %ERRORLEVEL%
                                echo [3/3] Kiem tra trang thai dich vu:
                                %COMPOSE_COMMAND% -p edumy -f ${composeFile} ps
                                exit /b %ERRORLEVEL%
                                :composeUnavailable
                                echo Docker Compose is not installed on this Jenkins agent.
                                exit /b 1
                            """
                        }
                    }

                    echo "==> Hoan tat quy trinh trien khai!"
                }
            }
        }

        stage('Deploy (Kubernetes)') {
            when {
                expression { return params.DEPLOY_APPLICATION && params.DEPLOY_TARGET == 'kubernetes' }
            }
            steps {
                script {
                    if (!params.BUILD_DOCKER_IMAGES || !params.PUSH_TO_REGISTRY) {
                        error('Triển khai Kubernetes cần BUILD_DOCKER_IMAGES và PUSH_TO_REGISTRY cùng được bật.')
                    }
                    def projDir = env.PROJECT_DIR ?: (fileExists('Edumy/k8s/edumy.yaml') ? 'Edumy' : '.')
                    def registry = params.DOCKER_REGISTRY
                    def dockerServer = registry.startsWith('docker.io/') ? 'https://index.docker.io/v1/' : registry.tokenize('/')[0]
                    def imagePrefix = registry
                    def tag = env.IMAGE_TAG

                    withCredentials([
                        file(credentialsId: params.KUBECONFIG_CREDENTIAL_ID, variable: 'KUBECONFIG'),
                        usernamePassword(
                            credentialsId: params.DOCKER_CREDENTIALS_ID,
                            usernameVariable: 'DOCKER_USER',
                            passwordVariable: 'DOCKER_PASSWORD'
                        )
                    ]) {
                        withEnv([
                            "DOCKER_SERVER=${dockerServer}",
                            "IMAGE_PREFIX=${imagePrefix}",
                            "IMAGE_TAG=${tag}"
                        ]) {
                            if (isUnix()) {
                                sh """
                                    set -eu
                                    echo '=== BƯỚC 5: Triển khai lên cụm Kubernetes ==='

                                    # 0. Tự động kiểm tra và cài đặt kubectl nếu chưa có trên Jenkins agent (lưu vào \$HOME/bin để tái sử dụng)
                                    if ! command -v kubectl >/dev/null 2>&1; then
                                        if [ -f "\$HOME/bin/kubectl" ]; then
                                            export PATH="\$HOME/bin:\$PATH"
                                        else
                                            echo "kubectl chưa có trên agent, đang tự động tải binary kubectl..."
                                            mkdir -p "\$HOME/bin"
                                            curl -sLo "\$HOME/bin/kubectl" "https://dl.k8s.io/release/v1.30.0/bin/linux/amd64/kubectl"
                                            chmod +x "\$HOME/bin/kubectl"
                                            export PATH="\$HOME/bin:\$PATH"
                                        fi
                                    fi
                                    echo "Sử dụng: \$(kubectl version --client --short 2>/dev/null || kubectl version --client)"

                                    # Điều chỉnh kubeconfig nếu Jenkins chạy trong Docker container kết nối tới host K8s (127.0.0.1 -> host.docker.internal)
                                    cp "\$KUBECONFIG" /tmp/kubeconfig
                                    export KUBECONFIG=/tmp/kubeconfig
                                    CURRENT_CLUSTER=\$(kubectl config view --minify -o jsonpath='{.clusters[0].name}')
                                    CURRENT_SERVER=\$(kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}')
                                    NEW_SERVER=\$(echo "\$CURRENT_SERVER" | sed -e 's|127.0.0.1|host.docker.internal|g' -e 's|localhost|host.docker.internal|g')
                                    if [ "\$CURRENT_SERVER" != "\$NEW_SERVER" ]; then
                                        echo "--> [Fix K8s Network] Chuyển Kubernetes API Server từ \$CURRENT_SERVER sang \$NEW_SERVER"
                                        kubectl config set-cluster "\$CURRENT_CLUSTER" --server="\$NEW_SERVER" --insecure-skip-tls-verify=true
                                    fi

                                    if ! kubectl cluster-info; then
                                        echo "Không thể kết nối Kubernetes API từ Jenkins agent. Hãy đảm bảo cluster đang chạy và endpoint trong kubeconfig truy cập được từ agent." >&2
                                        exit 1
                                    fi

                                    # 1. Khởi tạo Namespace
                                    kubectl apply -f ${projDir}/k8s/namespace.yaml

                                    # 2. Khởi tạo Secret (Docker Registry credentials & Secret runtime)
                                    kubectl get secret edumy-runtime -n edumy || true
                                    kubectl create secret docker-registry edumy-registry \\
                                      --namespace edumy \\
                                      --docker-server="\$DOCKER_SERVER" \\
                                      --docker-username="\$DOCKER_USER" \\
                                      --docker-password="\$DOCKER_PASSWORD" \\
                                      --dry-run=client -o yaml | kubectl apply -f -

                                    # 3. Khởi tạo Deployment, StatefulSet Database & Services cho Edumy
                                    kubectl apply -f ${projDir}/k8s/edumy.yaml

                                    # 4. Cập nhật image mới nhất cho Backend & Frontend (Rolling Update không gián đoạn)
                                    kubectl set image deployment/edumy-backend backend="\$IMAGE_PREFIX:backend-\$IMAGE_TAG" -n edumy || true
                                    kubectl set image deployment/edumy-frontend frontend="\$IMAGE_PREFIX:frontend-\$IMAGE_TAG" -n edumy || true

                                    # 5. Theo dõi tiến trình cập nhật Pod
                                    kubectl rollout status deployment/edumy-backend -n edumy --timeout=180s
                                    kubectl rollout status deployment/edumy-frontend -n edumy --timeout=180s
                                """
                            } else {
                                bat """
                                    @echo off
                                    kubectl cluster-info
                                    if %ERRORLEVEL% NEQ 0 (
                                        echo Khong the ket noi Kubernetes API tu Jenkins agent. Hay dam bao cluster dang chay va endpoint trong kubeconfig truy cap duoc tu agent.
                                        exit /b 1
                                    )
                                    kubectl apply -f ${projDir}/k8s/namespace.yaml
                                    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
                                    kubectl get secret edumy-runtime -n edumy
                                    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
                                    kubectl create secret docker-registry edumy-registry --namespace edumy --docker-server="%DOCKER_SERVER%" --docker-username="%DOCKER_USER%" --docker-password="%DOCKER_PASSWORD%" --dry-run=client -o yaml | kubectl apply -f -
                                    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
                                    kubectl apply -f ${projDir}/k8s/edumy.yaml
                                    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
                                    kubectl set image deployment/edumy-backend backend="%IMAGE_PREFIX%:backend-%IMAGE_TAG%" -n edumy
                                    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
                                    kubectl set image deployment/edumy-frontend frontend="%IMAGE_PREFIX%:frontend-%IMAGE_TAG%" -n edumy
                                    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
                                    kubectl rollout status deployment/edumy-backend -n edumy --timeout=5m
                                    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
                                    kubectl rollout status deployment/edumy-frontend -n edumy --timeout=5m
                                """
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                echo "--> [Cleanup] Dọn dẹp Docker dangling images và build cache cũ..."
                if (isUnix()) {
                    sh """
                        docker image prune -f || true
                        docker builder prune --filter "until=24h" -f || true
                    """
                } else {
                    bat """
                        @echo off
                        where docker >nul 2>&1
                        if %ERRORLEVEL% EQU 0 (
                            docker image prune -f
                            docker builder prune --filter "until=24h" -f
                        )
                    """
                }
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