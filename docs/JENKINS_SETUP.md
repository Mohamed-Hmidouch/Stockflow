# 🚀 Configuration Jenkins pour StockGestion

## 📋 Table des matières
- [Prérequis](#prérequis)
- [Installation de Jenkins](#installation-de-jenkins)
- [Configuration initiale](#configuration-initiale)
- [Configuration des plugins](#configuration-des-plugins)
- [Configuration du pipeline](#configuration-du-pipeline)
- [Configuration SonarQube](#configuration-sonarqube)
- [Troubleshooting](#troubleshooting)

---

## 🔧 Prérequis

### Logiciels requis
- ✅ Docker & Docker Compose
- ✅ Java JDK 17
- ✅ Maven 3.8+
- ✅ Git

### Services requis (via docker-compose)
```bash
# Démarrer tous les services
docker-compose up -d

# Services disponibles :
# - Jenkins: http://localhost:8081
# - SonarQube: http://localhost:9000
# - PostgreSQL: localhost:5433
# - PgAdmin: http://localhost:5051
```

---

## 🚀 Installation de Jenkins

### 1. Démarrer Jenkins via Docker Compose

Le service Jenkins est déjà configuré dans votre `compose.yaml` :

```bash
cd /home/mohamed-hmidouch/stockgestion
docker-compose up -d jenkins
```

### 2. Récupérer le mot de passe administrateur initial

```bash
docker exec stockgestion-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### 3. Accéder à Jenkins

Ouvrez votre navigateur : **http://localhost:8081**

1. Collez le mot de passe initial
2. Choisir "Install suggested plugins"
3. Créer un compte administrateur
4. Configurer l'URL Jenkins : `http://localhost:8081/`

---

## 🔌 Configuration des plugins

### Plugins essentiels à installer

Allez dans **Manage Jenkins** → **Manage Plugins** → **Available plugins**

#### Plugins obligatoires :
- ✅ **Git Plugin** (déjà installé normalement)
- ✅ **Pipeline** (déjà installé normalement)
- ✅ **Maven Integration**
- ✅ **Docker Pipeline**
- ✅ **SonarQube Scanner**
- ✅ **JaCoCo Plugin**
- ✅ **JUnit Plugin**

#### Plugins recommandés :
- 📊 **Blue Ocean** (interface moderne)
- 📈 **Build Metrics**
- 🔔 **Email Extension** (notifications)
- 🐙 **GitHub Integration**
- 📝 **Timestamper** (logs avec timestamps)

### Installation via CLI (optionnel)

```bash
docker exec stockgestion-jenkins jenkins-plugin-cli --plugins \
  git \
  workflow-aggregator \
  maven-plugin \
  docker-workflow \
  sonar \
  jacoco \
  junit \
  blueocean
```

---

## ⚙️ Configuration initiale

### 1. Configurer Maven

**Manage Jenkins** → **Global Tool Configuration** → **Maven**

1. Cliquer sur "Add Maven"
2. Nom : `Maven`
3. Version : choisir une version 3.8+
4. Cocher "Install automatically"
5. Sauvegarder

### 2. Configurer JDK

**Manage Jenkins** → **Global Tool Configuration** → **JDK**

1. Cliquer sur "Add JDK"
2. Nom : `JDK17`
3. Cocher "Install automatically"
4. Version : Java 17
5. Sauvegarder

### 3. Configurer Docker (si nécessaire)

Le Docker est déjà accessible via le socket monté dans `compose.yaml` :
```yaml
volumes:
  - /var/run/docker.sock:/var/run/docker.sock
```

Pour vérifier :
```bash
docker exec stockgestion-jenkins docker --version
```

---

## 📊 Configuration SonarQube

### 1. Démarrer SonarQube

```bash
docker-compose up -d sonarqube
```

Accéder à : **http://localhost:9000**

- **Login par défaut** : `admin`
- **Mot de passe par défaut** : `admin`
- Vous serez invité à changer le mot de passe

### 2. Générer un token SonarQube

1. Aller dans **My Account** → **Security** → **Generate Tokens**
2. Nom du token : `jenkins-token`
3. Type : `Global Analysis Token`
4. Copier le token généré (ex: `sqp_1234567890abcdef`)

### 3. Configurer SonarQube dans Jenkins

**Manage Jenkins** → **Configure System** → **SonarQube servers**

1. Cocher "Environment variables → Enable injection of SonarQube server configuration"
2. Cliquer "Add SonarQube"
3. Configuration :
   - **Name** : `SonarQube` ⚠️ **IMPORTANT : Le nom DOIT être exactement "SonarQube"** (avec S majuscule, Q majuscule)
   - **Server URL** : `http://sonarqube:9000`
   - **Server authentication token** : Cliquer "Add" → Jenkins

> 🚨 **ATTENTION CRITIQUE** : Le nom `SonarQube` est utilisé dans le Jenkinsfile à la ligne :
> ```groovy
> withSonarQubeEnv('SonarQube') {
> ```
> Si le nom ne correspond pas exactement, le build échouera à l'étape "🔍 Analyse SonarQube".

#### Ajouter le token :
- **Kind** : Secret text
- **Secret** : Coller votre token SonarQube
- **ID** : `sonarqube-token`
- **Description** : `SonarQube Authentication Token`
- Cliquer "Add"

4. Sélectionner le credential créé
5. Sauvegarder

### 4. Configurer le Scanner SonarQube

**Manage Jenkins** → **Global Tool Configuration** → **SonarQube Scanner**

1. Cliquer "Add SonarQube Scanner"
2. Nom : `SonarScanner`
3. Cocher "Install automatically"
4. Version : dernière version
5. Sauvegarder

---

## 🔨 Configuration du pipeline

### 1. Créer un nouveau job Pipeline

1. **New Item**
2. Nom : `StockGestion-Pipeline`
3. Type : **Pipeline**
4. Cliquer OK

### 2. Configuration du job

#### General
- ☑️ **GitHub project** : `https://github.com/Mohamed-Hmidouch/Stockflow`
- ☑️ **Discard old builds** : Garder les 10 derniers builds

#### Build Triggers
- ☑️ **GitHub hook trigger for GITScm polling** (si webhook configuré)
- ☑️ **Poll SCM** : `H/15 * * * *` (vérifier toutes les 15 min)

#### Pipeline
- **Definition** : Pipeline script from SCM
- **SCM** : Git
- **Repository URL** : `https://github.com/Mohamed-Hmidouch/Stockflow.git`
- **Credentials** : Ajouter vos credentials GitHub si nécessaire
- **Branch Specifier** : `*/SS-25-configuration-added-for-ci-cd` (ou votre branche)
- **Script Path** : `Jenkinsfile`

### 3. Sauvegarder et tester

Cliquer sur **Build Now** pour tester le pipeline.

---

## 🔐 Configuration des credentials (si nécessaire)

### GitHub Credentials

**Manage Jenkins** → **Manage Credentials** → **Global** → **Add Credentials**

- **Kind** : Username with password
- **Username** : Votre username GitHub
- **Password** : Personal Access Token GitHub
- **ID** : `github-credentials`
- **Description** : GitHub Access Token

### Docker Registry (optionnel)

Si vous utilisez un registry Docker privé :

- **Kind** : Username with password
- **Username** : Votre username Docker Hub
- **Password** : Votre token Docker Hub
- **ID** : `docker-credentials`
- **Description** : Docker Hub Credentials

---

## 📊 Visualisation des résultats

### Après chaque build, vous aurez :

1. **📋 Console Output** : Logs détaillés
2. **🧪 Test Results** : Résultats JUnit
3. **📈 JaCoCo Coverage** : Couverture de code
4. **🔍 SonarQube Analysis** : Qualité du code
5. **📦 Artifacts** : JAR archivé
6. **🐳 Docker Images** : Images construites

### Accéder aux rapports

- **Jenkins** : http://localhost:8081/job/StockGestion-Pipeline/
- **SonarQube** : http://localhost:9000/dashboard?id=stockgestion
- **JaCoCo** : http://localhost:8081/job/StockGestion-Pipeline/lastBuild/jacoco/

---

## 🔄 Workflow CI/CD

### Branches et déploiements

Le Jenkinsfile est configuré pour :

| Branch | Tests | Build Docker | Deploy |
|--------|-------|--------------|--------|
| `feature/*` | ✅ | ❌ | ❌ |
| `SS-*` | ✅ | ✅ | ❌ |
| `develop` | ✅ | ✅ | ✅ Dev |
| `main/master` | ✅ | ✅ | ✅ Prod (manuel) |

### Étapes du pipeline

```
📦 Checkout
   ↓
🧹 Clean
   ↓
🔍 Compile
   ↓
🧪 Tests Unitaires
   ↓
📦 Package (JAR)
   ↓
📊 Analyse Couverture (JaCoCo)
   ↓
🔍 Analyse SonarQube
   ↓
🚦 Quality Gate
   ↓
🐳 Build Docker Image
   ↓
🧪 Test Docker Image
   ↓
📤 Push Image (main/master)
   ↓
🚀 Deploy (develop/main)
```

---

## 🐛 Troubleshooting

### Problème 1 : Maven non trouvé

**Erreur** : `mvn: command not found`

**Solution** :
- Vérifier la configuration Maven dans Global Tool Configuration
- Ou installer Maven dans le conteneur Jenkins :
```bash
docker exec -u root stockgestion-jenkins apt-get update
docker exec -u root stockgestion-jenkins apt-get install -y maven
```

### Problème 2 : Docker permission denied

**Erreur** : `permission denied while trying to connect to Docker daemon socket`

**Solution** :
```bash
docker exec -u root stockgestion-jenkins chmod 666 /var/run/docker.sock
```

### Problème 3 : SonarQube Quality Gate timeout

**Erreur** : `Timeout waiting for quality gate`

**Solution** :
- Vérifier que SonarQube est accessible : http://localhost:9000
- Augmenter le timeout dans le Jenkinsfile (ligne ~145)
- Vérifier les logs SonarQube : `docker logs stockgestion-sonarqube`

### Problème 4 : Erreur "SonarQube server 'SonarQube' not found"

**Erreur** : `No SonarQube server configured with name 'SonarQube'`

**Solution** :
```
🚨 CRITIQUE : Le nom du serveur SonarQube dans Jenkins DOIT être exactement "SonarQube"

1. Aller dans Manage Jenkins → Configure System
2. Section "SonarQube servers"
3. Vérifier que le champ "Name" est bien "SonarQube" (S majuscule, Q majuscule)
4. Ne pas utiliser "Mon-SonarQube", "sonarqube", "SonarQube-Server" ou autre variante
5. Sauvegarder et relancer le build

Le nom correspond au code dans Jenkinsfile ligne ~125 :
withSonarQubeEnv('SonarQube') {
```

### Problème 5 : Tests échouent avec base de données

**Erreur** : `Connection refused: postgres`

**Solution** :
- Vérifier que PostgreSQL est démarré : `docker ps | grep postgres`
- Les tests utilisent une base H2 en mémoire ou PostgreSQL de test
- Vérifier `application-test.properties` si nécessaire

### Problème 6 : JaCoCo plugin non trouvé

**Solution** :
```bash
docker exec stockgestion-jenkins jenkins-plugin-cli --plugins jacoco
docker restart stockgestion-jenkins
```

---

## 📚 Ressources utiles

### Documentation
- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [SonarQube Documentation](https://docs.sonarqube.org/)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)

### Commandes utiles

```bash
# Vérifier l'état des services
docker-compose ps

# Voir les logs Jenkins
docker logs -f stockgestion-jenkins

# Voir les logs SonarQube
docker logs -f stockgestion-sonarqube

# Redémarrer Jenkins
docker restart stockgestion-jenkins

# Nettoyer les images Docker
docker system prune -a

# Backup Jenkins data
docker run --rm -v jenkins-data:/data -v $(pwd):/backup alpine tar czf /backup/jenkins-backup.tar.gz /data
```

---

## ✅ Checklist de configuration

- [ ] Jenkins démarré et accessible (http://localhost:8081)
- [ ] Plugins installés (Maven, Docker, SonarQube, JaCoCo)
- [ ] Maven configuré dans Global Tool Configuration
- [ ] JDK 17 configuré
- [ ] SonarQube démarré (http://localhost:9000)
- [ ] Token SonarQube créé et configuré dans Jenkins
- [ ] Pipeline créé et configuré
- [ ] GitHub repository lié
- [ ] Premier build réussi
- [ ] Tests unitaires passent
- [ ] Rapport JaCoCo généré
- [ ] Analyse SonarQube complétée
- [ ] Image Docker construite

---

## 🎯 Prochaines étapes

1. **Configurer les webhooks GitHub** pour déclencher automatiquement les builds
2. **Ajouter des tests d'intégration** dans le pipeline
3. **Configurer les notifications** (email, Slack, etc.)
4. **Mettre en place un environnement de staging**
5. **Configurer le versioning sémantique** (semantic release)
6. **Ajouter des health checks** après déploiement

---

**📝 Dernière mise à jour** : 12 Novembre 2025  
**👤 Auteur** : Configuration pour StockGestion Project  
**🔖 Version** : 1.0.0
