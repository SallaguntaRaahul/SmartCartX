pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'smartcart-app'
        DOCKER_TAG = "${BUILD_NUMBER}"
        ACR_URL = 'smartcartxraahul.azurecr.io'
    }

    stages {

        stage('Checkout') {
            steps {
                echo '=== Checking out source code ==='
                checkout scm
                sh 'git log --oneline -3'
            }
        }

        stage('Build') {
            steps {
                echo '=== Building SmartCartX ==='
                sh './mvnw clean compile -B'
            }
        }

        stage('Unit Tests') {
            steps {
                echo '=== Running Unit Tests ==='
                sh './mvnw test -B'
            }
            post {
                always {
                    junit(
                        testResults: '**/target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }

        stage('Code Coverage') {
            steps {
                echo '=== Generating JaCoCo Coverage Report ==='
                sh './mvnw jacoco:report -B'
            }
        }

        stage('Package') {
            steps {
                echo '=== Packaging JAR ==='
                sh './mvnw package -DskipTests -B'
                sh 'ls -la target/*.jar'
            }
        }

        stage('Docker Build') {
            steps {
                echo '=== Building Docker Image ==='
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} -t ${DOCKER_IMAGE}:latest ."
                sh "docker images | grep ${DOCKER_IMAGE}"
            }
        }

        stage('Verify Kubernetes') {
            steps {
                echo '=== Verifying Kubernetes Files ==='
                sh 'ls -la k8s/'
                sh 'ls -la helm/smartcartx/'
                echo 'Kubernetes manifests ready for deployment ✅'
            }
        }

        stage('Summary') {
            steps {
                echo """
                ================================
                SMARTCARTX BUILD SUMMARY
                ================================
                Build:  ${BUILD_NUMBER}
                Image:  ${DOCKER_IMAGE}:${DOCKER_TAG}
                K8s:    manifests ready
                Helm:   chart ready
                ================================
                """
            }
        }
    }

    post {
        success {
            echo 'SMARTCARTX PIPELINE SUCCESS ✅'
        }
        failure {
            echo 'SMARTCARTX PIPELINE FAILED ❌'
        }
        always {
            sh "docker rmi ${DOCKER_IMAGE}:${BUILD_NUMBER} || true"
        }
    }
}
