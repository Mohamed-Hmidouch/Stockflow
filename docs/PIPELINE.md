# 🚀 Pipeline CI/CD - StockGestion

## 📊 Vue d'ensemble

Ce projet utilise Jenkins pour l'intégration et le déploiement continus (CI/CD) avec les outils suivants :

- **🔨 Jenkins** : Orchestration du pipeline
- **📊 SonarQube** : Analyse de la qualité du code
- **📈 JaCoCo** : Couverture de code
- **🐳 Docker** : Containerisation
- **☕ Maven** : Build et gestion des dépendances

---

## 🏗️ Architecture du Pipeline

```
┌─────────────────────────────────────────────────────────────┐
│                     JENKINSFILE PIPELINE                     │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
   ┌─────────┐        ┌─────────┐        ┌─────────┐
   │  Build  │        │  Test   │        │ Deploy  │
   │  Stage  │        │  Stage  │        │  Stage  │
   └─────────┘        └─────────┘        └─────────┘
        │                   │                   │
        ▼                   ▼                   ▼
   Maven Build      Unit Tests         Docker Deploy
   Docker Build     Coverage           Environment
   Quality Gate     SonarQube          Health Check
```

---

## 📋 Stages du Pipeline

### 1️⃣ Checkout (📦)
- Récupération du code source depuis Git
- Extraction des informations de commit et branche

### 2️⃣ Clean (🧹)
- Nettoyage de l'environnement de build
- Suppression des artefacts précédents

### 3️⃣ Compile (🔍)
- Compilation du code source Java
- Vérification de la syntaxe et des dépendances

### 4️⃣ Tests Unitaires (🧪)
- Exécution des tests unitaires avec JUnit
- Publication des résultats de tests
- **31 tests** actuellement dans le projet

### 5️⃣ Package (📦)
- Création du fichier JAR exécutable
- Archivage de l'artefact
- **Output** : `stockgestion-0.0.1-SNAPSHOT.jar`

### 6️⃣ Analyse de Couverture (📊)
- Génération du rapport JaCoCo
- Calcul de la couverture de code
- Publication du rapport dans Jenkins

### 7️⃣ Analyse SonarQube (🔍)
- Analyse statique du code
- Détection des bugs et vulnérabilités
- Calcul de la dette technique

### 8️⃣ Quality Gate (🚦)
- Vérification des seuils de qualité
- **Bloque le pipeline** si la qualité est insuffisante

### 9️⃣ Build Docker Image (🐳)
- Construction de l'image Docker
- Tagging avec version et commit
- **Branches** : main, master, develop, SS-*

### 🔟 Test Docker Image (🧪)
- Vérification de l'image construite
- Test de présence du JAR

### 1️⃣1️⃣ Push Docker Image (📤)
- Publication vers le registry (optionnel)
- **Branches** : main, master uniquement

### 1️⃣2️⃣ Deploy (🚀)
- **Develop** : Déploiement automatique en dev
- **Main/Master** : Déploiement manuel en prod avec confirmation

---

## ⚙️ Variables d'environnement

```groovy
MAVEN_HOME          : Chemin vers Maven
DOCKER_IMAGE        : Nom de l'image Docker (stockgestion-app)
DOCKER_TAG          : Numéro de build
SONAR_HOST_URL      : URL SonarQube (http://sonarqube:9000)
SONAR_PROJECT_KEY   : Clé du projet (stockgestion)
DB_URL              : URL PostgreSQL pour tests
```

---

## 🔀 Stratégie de branches

### Branches et comportements

| Branche | Tests | Build | Docker | Deploy |
|---------|-------|-------|--------|--------|
| `feature/*` | ✅ | ✅ | ❌ | ❌ |
| `SS-*` (tickets) | ✅ | ✅ | ✅ | ❌ |
| `develop` | ✅ | ✅ | ✅ | ✅ Dev |
| `main`/`master` | ✅ | ✅ | ✅ | ✅ Prod* |

*Déploiement en production nécessite une confirmation manuelle

---

## 📊 Métriques et Rapports

### JaCoCo Coverage
- **Emplacement** : `target/site/jacoco/`
- **Formats** : HTML, XML, CSV
- **Accessible via** : Jenkins → Build → JaCoCo Coverage

### SonarQube Analysis
- **URL** : http://localhost:9000
- **Project** : stockgestion
- **Métriques** :
  - Code Smells
  - Bugs
  - Vulnerabilities
  - Duplications
  - Coverage

### JUnit Tests
- **Résultats** : `target/surefire-reports/`
- **Format** : XML
- **Tests totaux** : 31
- **Accessible via** : Jenkins → Build → Test Results

---

## 🐳 Images Docker

### Tags créés

Chaque build crée 3 tags :

```bash
stockgestion-app:${BUILD_NUMBER}      # Ex: stockgestion-app:42
stockgestion-app:${GIT_COMMIT_SHORT}  # Ex: stockgestion-app:a1b2c3d
stockgestion-app:latest               # Latest build
```

### Vérifier les images locales

```bash
docker images stockgestion-app
```

---

## 🚀 Déploiement

### Environnement de développement (develop)

Déploiement **automatique** après quality gate :

```bash
# Arrêt de l'ancienne version
docker stop stockgestion-app
docker rm stockgestion-app

# Démarrage de la nouvelle version
docker-compose up -d stockgestion-app
```

### Environnement de production (main/master)

Déploiement **manuel** avec confirmation :

1. Le pipeline s'arrête et demande confirmation
2. Un utilisateur autorisé clique sur "Déployer"
3. Le déploiement s'effectue avec health check

```bash
# Health check après déploiement
curl -f http://localhost:8080/actuator/health
```

---

## 🔔 Notifications (à configurer)

Le pipeline supporte les notifications par :

- ✉️ **Email** : Via Email Extension Plugin
- 💬 **Slack** : Via Slack Notification Plugin
- 🐙 **GitHub** : Statuts de commit

### Exemple de notification email

```groovy
emailext (
    subject: "✅ Build Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
    body: "Le build a réussi. Consultez les détails: ${env.BUILD_URL}",
    to: "team@example.com"
)
```

---

## 🛠️ Commandes utiles

### Lancer un build manuellement

```bash
# Via interface Jenkins
# Cliquer sur "Build Now"

# Via API (avec crumb)
curl -X POST http://localhost:8081/job/StockGestion-Pipeline/build \
  --user admin:your-token
```

### Vérifier le statut du dernier build

```bash
# Logs du dernier build
docker exec stockgestion-jenkins \
  cat /var/jenkins_home/jobs/StockGestion-Pipeline/builds/lastSuccessfulBuild/log

# Ou via l'interface web
http://localhost:8081/job/StockGestion-Pipeline/lastBuild/console
```

### Nettoyer les anciens builds

```bash
# Les builds sont automatiquement nettoyés
# Configuration : Garder les 10 derniers builds
# Voir : buildDiscarder(logRotator(numToKeepStr: '10'))
```

---

## 🐛 Résolution de problèmes

### Build échoue au stage "Compile"

**Causes possibles** :
- Erreur de syntaxe Java
- Dépendance Maven manquante
- Version Java incorrecte

**Solution** :
```bash
# Vérifier localement
mvn clean compile
```

### Build échoue au stage "Tests"

**Causes possibles** :
- Test unitaire échoue
- Base de données non accessible
- Configuration incorrecte

**Solution** :
```bash
# Exécuter les tests localement
mvn test

# Voir les rapports
cat target/surefire-reports/*.txt
```

### Quality Gate échoue

**Causes possibles** :
- Couverture de code insuffisante
- Bugs/vulnérabilités détectés
- Code smell trop élevé

**Solution** :
1. Consulter SonarQube : http://localhost:9000
2. Corriger les issues remontées
3. Relancer le build

### Docker build échoue

**Causes possibles** :
- JAR non créé
- Dockerfile incorrect
- Problème de permissions

**Solution** :
```bash
# Vérifier que le JAR existe
ls -lh target/*.jar

# Tester le build Docker localement
docker build -t test-build .
```

---

## 📈 Optimisations possibles

### 1. Build parallèle
```groovy
stage('Tests') {
    parallel {
        stage('Unit Tests') { ... }
        stage('Integration Tests') { ... }
    }
}
```

### 2. Cache Maven
```groovy
options {
    skipStagesAfterUnstable()
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '10'))
}
```

### 3. Multi-branch pipeline
- Créer un pipeline multi-branches
- Détection automatique des branches
- Configuration par branche

---

## 📚 Documentation complémentaire

- 📖 [Guide de configuration Jenkins](./JENKINS_SETUP.md)
- 🐳 [Documentation Docker](../Dockerfile)
- 📊 [Configuration SonarQube](../pom.xml)
- ☕ [Configuration Maven](../pom.xml)

---

## ✅ Checklist avant commit

- [ ] Tests unitaires passent localement (`mvn test`)
- [ ] Build Maven réussit (`mvn clean package`)
- [ ] Pas d'erreur SonarQube critique
- [ ] Couverture de code > 80% (recommandé)
- [ ] Code formaté correctement
- [ ] Commit message descriptif

---

## 🎯 Métriques de qualité actuelles

### Tests
- ✅ **31 tests** passent
- ✅ **0 failures**
- ✅ **0 errors**

### Couverture (JaCoCo)
- 📊 **46 classes** analysées
- 📈 Rapport disponible dans `target/site/jacoco/`

### Build
- ⏱️ Temps moyen : ~26 secondes (local)
- 📦 Artefact : `stockgestion-0.0.1-SNAPSHOT.jar`
- 🐳 Image Docker : `stockgestion-app:latest`

---

**📝 Maintenu par** : L'équipe StockGestion  
**📅 Dernière mise à jour** : 12 Novembre 2025  
**🔖 Version Pipeline** : 1.0.0
