pipeline {
    agent any

    environment {
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk'
        MAVEN_HOME = '/usr/share/maven'
        DOCKER_IMAGE = 'smartcart-app'
        DOCKER_TAG = "${BUILD_NUMBER}"
        ACR_NAME = 'smartcartxraahul'
        ACR_URL = 'smartcartxraahul.azurecr.io'
    }

    tools {
        jdk 'JDK17'
        maven 'Maven3'
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
                sh 'mvn clean compile -B'
            }
        }

        stage('Unit Tests') {
            steps {
                echo '=== Running Unit Tests ==='
                sh '''
                    mvn test \
                        -DSPRING_DATASOURCE_URL=jdbc:h2:mem:testdb \
                        -DSPRING_DATASOURCE_USERNAME=sa \
                        -DSPRING_DATASOURCE_PASSWORD= \
                        -DSPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
                        -DSPRING_DATA_REDIS_HOST=localhost
                '''
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Coverage') {
            steps {
                echo '=== Generating JaCoCo Coverage Report ==='
                sh 'mvn jacoco:report'
            }
            post {
                always {
                    jacoco(
                        execPattern: 'target/jacoco.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java',
                        exclusionPattern: 'src/test*'
                    )
                }
            }
        }

        stage('Package') {
            steps {
                echo '=== Packaging JAR ==='
                sh 'mvn package -DskipTests'
                sh 'ls -la target/*.jar'
            }
        }

        stage('Docker Build') {
            steps {
                echo '=== Building Docker Image ==='
                sh """
                    docker build \
                        -t ${DOCKER_IMAGE}:${DOCKER_TAG} \
                        -t ${DOCKER_IMAGE}:latest \
                        .
                """
                sh "docker images | grep ${DOCKER_IMAGE}"
            }
        }

        stage('Docker Push to ACR') {
            steps {
                echo '=== Pushing to Azure Container Registry ==='
                withCredentials([usernamePassword(
                    credentialsId: 'acr-credentials',
                    usernameVariable: 'ACR_USER',
                    passwordVariable: 'ACR_PASSWORD'
                )]) {
                    sh """
                        docker login ${ACR_URL} \
                            -u ${ACR_USER} \
                            -p ${ACR_PASSWORD}
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} \
                            ${ACR_URL}/${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker tag ${DOCKER_IMAGE}:latest \
                            ${ACR_URL}/${DOCKER_IMAGE}:latest
                        docker push ${ACR_URL}/${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker push ${ACR_URL}/${DOCKER_IMAGE}:latest
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                echo '=== Deploying to Kubernetes ==='
                withCredentials([file(
                    credentialsId: 'kubeconfig',
                    variable: 'KUBECONFIG'
                )]) {
                    sh """
                        kubectl apply -f k8s/namespace.yaml
                        kubectl apply -f k8s/configmap.yaml
                        kubectl apply -f k8s/secret.yaml
                        kubectl apply -f k8s/postgres.yaml
                        kubectl apply -f k8s/redis.yaml
                        kubectl apply -f k8s/kafka.yaml
                        kubectl apply -f k8s/app.yaml
                        kubectl rollout status \
                            deployment/smartcart-app \
                            -n smartcart \
                            --timeout=300s
                    """
                }
            }
        }

        stage('Smoke Test') {
            steps {
                echo '=== Running Smoke Tests ==='
                sh '''
                    sleep 30
                    kubectl get pods -n smartcart
                    kubectl get services -n smartcart
                '''
            }
        }
    }

    post {
        success {
            echo """
            ================================
            SMARTCARTX PIPELINE SUCCESS ✅
            Build: ${BUILD_NUMBER}
            Branch: ${GIT_BRANCH}
            ================================
            """
        }
        failure {
            echo """
            ================================
            SMARTCARTX PIPELINE FAILED ❌
            Build: ${BUILD_NUMBER}
            Branch: ${GIT_BRANCH}
            ================================
            """
        }
        always {
            echo '=== Cleaning up ==='
            sh "docker rmi ${DOCKER_IMAGE}:${BUILD_NUMBER} || true"
            cleanWs()
        }
    }
}
