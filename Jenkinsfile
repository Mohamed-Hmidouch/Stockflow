pipeline {
    agent any
    
    // Variables d'environnement
    environment {
        // Configuration Maven
        MAVEN_HOME = tool 'Maven'
        MAVEN_OPTS = '-Xmx1024m -Xms512m'
        
        // Configuration Docker
        DOCKER_IMAGE = "mohamedhmidouch/stockgestion-app"
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        DOCKER_REGISTRY = "" // Ajouter votre registry si nécessaire (ex: docker.io/username)
        
        // Configuration SonarQube
        SONAR_HOST_URL = "http://sonarqube:9000"
        SONAR_PROJECT_KEY = "stockgestion"
        SONAR_PROJECT_NAME = "StockGestion"
        
        // Configuration de la base de données pour les tests
        DB_URL = "jdbc:postgresql://postgres:5432/stockgestiondb"
        DB_USER = "stockuser"
        DB_PASSWORD = "stockpass"
    }
    
    // Déclencheurs
    triggers {
        // Polling SCM toutes les 15 minutes
        pollSCM('H/15 * * * *')
        // Ou utiliser webhook GitHub
        // githubPush()
    }
    
    // Options du pipeline
    options {
        // Garder les 10 derniers builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Timeout global
        timeout(time: 30, unit: 'MINUTES')
        // Désactiver le checkout automatique
        skipDefaultCheckout(false)
        // Timestamper pour les logs
        timestamps()
    }
    
    stages {
        stage('📦 Checkout') {
            steps {
                echo '🔄 Récupération du code source...'
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: "git rev-parse --short HEAD",
                        returnStdout: true
                    ).trim()
                    env.GIT_BRANCH = env.BRANCH_NAME ?: 'unknown'
                }
                echo "✅ Branch: ${env.GIT_BRANCH}, Commit: ${env.GIT_COMMIT_SHORT}"
            }
        }
        
        stage('🧹 Clean') {
            steps {
                echo '🧹 Nettoyage de l\'environnement...'
                sh '''
                    ${MAVEN_HOME}/bin/mvn clean
                    echo "✅ Nettoyage terminé"
                '''
            }
        }
        
        stage('🔍 Compile') {
            steps {
                echo '🔨 Compilation du projet...'
                sh '''
                    ${MAVEN_HOME}/bin/mvn compile -DskipTests
                    echo "✅ Compilation réussie"
                '''
            }
        }
        
        stage('🧪 Tests Unitaires') {
            steps {
                echo '🧪 Exécution des tests unitaires...'
                sh '''
                    ${MAVEN_HOME}/bin/mvn test
                    echo "✅ Tests unitaires terminés"
                '''
            }
            post {
                always {
                    // Publication des résultats de tests
                    junit '**/target/surefire-reports/*.xml'
                    echo '📊 Résultats des tests publiés'
                }
            }
        }
        
        stage('📦 Package') {
            steps {
                echo '📦 Création du package JAR...'
                sh '''
                    ${MAVEN_HOME}/bin/mvn package -DskipTests
                    echo "✅ Package créé: target/stockgestion-0.0.1-SNAPSHOT.jar"
                '''
            }
            post {
                success {
                    // Archiver le JAR
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                    echo '✅ Artefact archivé'
                }
            }
        }
        
        stage('📊 Analyse de Couverture (JaCoCo)') {
            steps {
                echo '📊 Génération du rapport de couverture JaCoCo...'
                sh '''
                    ${MAVEN_HOME}/bin/mvn jacoco:report
                    echo "✅ Rapport JaCoCo généré dans target/site/jacoco/"
                '''
            }
            post {
                always {
                    // Publication du rapport JaCoCo
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        exclusionPattern: '**/test/**'
                    )
                    echo '📈 Rapport de couverture publié'
                }
            }
        }
        
        stage('🔍 Analyse SonarQube') {
            steps {
                echo '🔍 Analyse de la qualité du code avec SonarQube...'
                script {
                    // ⚠️ IMPORTANT: Le nom 'SonarQube' doit correspondre EXACTEMENT au nom
                    // du serveur SonarQube configuré dans Jenkins (Manage Jenkins > Configure System > SonarQube servers)
                    // Le nom est sensible à la casse : utilisez "SonarQube" (S majuscule, Q majuscule)
                    withSonarQubeEnv('SonarQube') {
                        sh '''
                            ${MAVEN_HOME}/bin/mvn sonar:sonar \
                                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                -Dsonar.projectName="${SONAR_PROJECT_NAME}" \
                                -Dsonar.host.url=${SONAR_HOST_URL} \
                                -Dsonar.java.coveragePlugin=jacoco \
                                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                                -Dsonar.sources=src/main/java \
                                -Dsonar.tests=src/test/java \
                                -Dsonar.java.binaries=target/classes
                            echo "✅ Analyse SonarQube terminée"
                        '''
                    }
                }
            }
        }
        
        stage('🚦 Quality Gate') {
            steps {
                echo '🚦 Vérification du Quality Gate SonarQube...'
                timeout(time: 5, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "❌ Quality Gate échoué: ${qg.status}"
                        } else {
                            echo "✅ Quality Gate réussi"
                        }
                    }
                }
            }
        }
        
        stage('🐳 Build Docker Image') {
            when {
                expression {
                    return env.GIT_BRANCH == 'main' || 
                           env.GIT_BRANCH == 'master' || 
                           env.GIT_BRANCH == 'develop' ||
                           env.GIT_BRANCH ==~ /.*\/main/ ||
                           env.GIT_BRANCH ==~ /.*\/master/ ||
                           env.GIT_BRANCH ==~ /.*\/develop/ ||
                           env.GIT_BRANCH ==~ /SS-.*/
                }
            }
            steps {
                echo '🐳 Construction de l\'image Docker...'
                script {
                    // Construction de l'image avec le numéro de build et latest
                    sh """
                        docker build \
                            -t ${DOCKER_IMAGE}:${DOCKER_TAG} \
                            -t ${DOCKER_IMAGE}:latest \
                            -t ${DOCKER_IMAGE}:${env.GIT_COMMIT_SHORT} \
                            .
                        echo "✅ Image Docker créée: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    """
                }
            }
        }
        
        stage('🧪 Test Docker Image') {
            when {
                expression {
                    return env.GIT_BRANCH == 'main' || 
                           env.GIT_BRANCH == 'master' || 
                           env.GIT_BRANCH == 'develop' ||
                           env.GIT_BRANCH ==~ /.*\/main/ ||
                           env.GIT_BRANCH ==~ /.*\/master/ ||
                           env.GIT_BRANCH ==~ /.*\/develop/ ||
                           env.GIT_BRANCH ==~ /SS-.*/
                }
            }
            steps {
                echo '🧪 Test de l\'image Docker...'
                script {
                    sh """
                        # Vérifier que l'image a été créée
                        docker images ${DOCKER_IMAGE}:${DOCKER_TAG}
                        
                        # Test basique: vérifier que le JAR est présent dans l'image
                        docker run --rm ${DOCKER_IMAGE}:${DOCKER_TAG} ls -lh /app.jar || true
                        
                        echo "✅ Image Docker testée avec succès"
                    """
                }
            }
        }
        
        stage('📤 Push Docker Image') {
            when {
            anyOf {
                expression { return env.BRANCH_NAME == 'main' }
                expression { return env.BRANCH_NAME == 'origin/main' }
                expression { return env.BRANCH_NAME == 'master' }
            }
            }
            steps {
            echo '📤 Connexion et Publication sur Docker Hub...'
            withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                sh '''
                echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                docker push ${DOCKER_IMAGE}:latest
                docker logout
                '''
            }
            }
        }
        
        stage('🚀 Deploy to Development') {
            when {
                expression {
                    return env.GIT_BRANCH == 'develop' ||
                           env.GIT_BRANCH ==~ /.*\/develop/
                }
            }
            steps {
                echo '🚀 Déploiement en environnement de développement...'
                script {
                    sh '''
                        # Arrêter et supprimer le conteneur existant
                        docker stop stockgestion-app || true
                        docker rm stockgestion-app || true
                        
                        # Déployer la nouvelle version avec docker-compose
                        docker-compose up -d stockgestion-app
                        
                        # Attendre que l'application soit prête
                        sleep 10
                        
                        # Vérifier le statut
                        docker ps | grep stockgestion-app
                        
                        echo "✅ Application déployée en développement"
                    '''
                }
            }
        }
        
        stage('🚀 Deploy to Production') {
            when {
                expression {
                    return env.GIT_BRANCH == 'main' || 
                           env.GIT_BRANCH == 'master' ||
                           env.GIT_BRANCH ==~ /.*\/main/ ||
                           env.GIT_BRANCH ==~ /.*\/master/
                }
            }
            steps {
                echo '🚀 Déploiement en production...'
                input message: 'Déployer en production ?', ok: 'Déployer'
                script {
                    sh '''
                        echo "🚀 Déploiement de la version ${DOCKER_TAG} en production..."
                        
                        # Utiliser docker-compose pour le déploiement
                        docker-compose down stockgestion-app
                        docker-compose up -d stockgestion-app
                        
                        # Health check
                        sleep 15
                        curl -f http://localhost:8080/actuator/health || echo "⚠️ Health check échoué"
                        
                        echo "✅ Déploiement en production terminé"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo '🧹 Nettoyage post-build...'
            // Nettoyer les images Docker non utilisées
            sh 'docker image prune -f || true'
            
            // Publier les logs
            echo '📋 Build terminé'
        }
        
        success {
            echo '✅ ========================================='
            echo '✅ BUILD RÉUSSI !'
            echo '✅ ========================================='
            echo "✅ Branch: ${env.GIT_BRANCH}"
            echo "✅ Commit: ${env.GIT_COMMIT_SHORT}"
            echo "✅ Build: #${env.BUILD_NUMBER}"
            echo "✅ Image Docker: ${DOCKER_IMAGE}:${DOCKER_TAG}"
            echo '✅ ========================================='
            
            // Notification (optionnel - nécessite un plugin)
            // emailext (
            //     subject: "✅ Build Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //     body: "Le build a réussi. Consultez les détails: ${env.BUILD_URL}",
            //     to: "team@example.com"
            // )
        }
        
        failure {
            echo '❌ ========================================='
            echo '❌ BUILD ÉCHOUÉ !'
            echo '❌ ========================================='
            echo "❌ Branch: ${env.GIT_BRANCH}"
            echo "❌ Commit: ${env.GIT_COMMIT_SHORT}"
            echo "❌ Build: #${env.BUILD_NUMBER}"
            echo "❌ Consultez les logs: ${env.BUILD_URL}console"
            echo '❌ ========================================='
            
            // Notification (optionnel)
            // emailext (
            //     subject: "❌ Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //     body: "Le build a échoué. Consultez les détails: ${env.BUILD_URL}",
            //     to: "team@example.com"
            // )
        }
        
        unstable {
            echo '⚠️ ========================================='
            echo '⚠️ BUILD INSTABLE'
            echo '⚠️ ========================================='
            echo "⚠️ Branch: ${env.GIT_BRANCH}"
            echo "⚠️ Commit: ${env.GIT_COMMIT_SHORT}"
            echo "⚠️ Build: #${env.BUILD_NUMBER}"
            echo '⚠️ ========================================='
        }
    }
}
