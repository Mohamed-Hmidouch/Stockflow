# ⚡ Quick Start Guide - StockGestion

Guide de démarrage rapide en 5 minutes !

---

## 🚀 Démarrage ultra-rapide

### Option 1 : Script automatique (Recommandé)

```bash
# 1. Rendre le script exécutable (une seule fois)
chmod +x cicd-manager.sh

# 2. Lancer le script
./cicd-manager.sh

# 3. Choisir l'option 1 (Démarrage complet)
```

✅ C'est tout ! Tous les services démarrent automatiquement.

---

### Option 2 : Docker Compose manuel

```bash
# Démarrer tous les services
docker-compose up -d

# Vérifier le statut
docker-compose ps

# Voir les logs
docker-compose logs -f stockgestion-app
```

---

### Option 3 : Développement local (sans Docker pour l'app)

```bash
# 1. Démarrer uniquement PostgreSQL
docker-compose up -d postgres

# 2. Build l'application
mvn clean package

# 3. Lancer l'application
java -jar target/stockgestion-0.0.1-SNAPSHOT.jar
```

---

## 🌐 Accéder aux services

Une fois démarrés, les services sont disponibles ici :

| Service | URL | Description |
|---------|-----|-------------|
| 🌐 **Application** | http://localhost:8080 | API REST |
| 📘 **Swagger UI** | http://localhost:8080/swagger-ui.html | Documentation interactive |
| 🔨 **Jenkins** | http://localhost:8081 | Pipeline CI/CD |
| 📊 **SonarQube** | http://localhost:9000 | Qualité du code |
| 🗄️ **PgAdmin** | http://localhost:5051 | Gestion PostgreSQL |

---

## ✅ Vérification rapide

### 1. Tester l'API

```bash
# Health check
curl http://localhost:8080/actuator/health

# Lister les produits
curl http://localhost:8080/api/products

# Créer un produit
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "sku": "TEST-001",
    "description": "Produit de test",
    "price": 99.99
  }'
```

### 2. Vérifier Jenkins

```bash
# Récupérer le mot de passe initial
docker exec stockgestion-jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# Copier le mot de passe et aller sur http://localhost:8081
```

### 3. Accéder à SonarQube

```
URL: http://localhost:9000
Login: admin
Password: admin
(Vous serez invité à changer le mot de passe)
```

---

## 🧪 Premier test complet

### 1. Exécuter les tests

```bash
mvn test
```

### 2. Générer le rapport de couverture

```bash
mvn jacoco:report

# Ouvrir le rapport
xdg-open target/site/jacoco/index.html  # Linux
# ou
open target/site/jacoco/index.html      # macOS
# ou
start target/site/jacoco/index.html     # Windows
```

### 3. Analyser avec SonarQube

```bash
mvn clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=admin \
  -Dsonar.password=votre-nouveau-mot-de-passe
```

---

## 🔨 Premier build Jenkins

### Configuration minimale

1. **Aller sur Jenkins** : http://localhost:8081

2. **Créer un nouveau job** :
   - New Item → Pipeline
   - Nom : `StockGestion-Pipeline`

3. **Configuration** :
   - Pipeline → Definition : `Pipeline script from SCM`
   - SCM : `Git`
   - Repository URL : `https://github.com/Mohamed-Hmidouch/Stockflow.git`
   - Branch : `*/SS-25-configuration-added-for-ci-cd`
   - Script Path : `Jenkinsfile`

4. **Sauvegarder et lancer** :
   - Cliquer sur "Build Now"
   - Voir les logs en temps réel

---

## 📊 Dashboard et métriques

### Swagger UI - API Documentation

http://localhost:8080/swagger-ui.html

- 📋 Tous les endpoints disponibles
- 🧪 Test direct des APIs
- 📖 Schémas des modèles

### Jenkins - Pipeline

http://localhost:8081/job/StockGestion-Pipeline/

- 📈 Historique des builds
- 🧪 Résultats des tests
- 📊 Couverture JaCoCo
- 📦 Artefacts générés

### SonarQube - Qualité du code

http://localhost:9000/dashboard?id=stockgestion

- 🐛 Bugs détectés
- 🔒 Vulnérabilités
- 📊 Couverture de code
- 💰 Dette technique

---

## 🛠️ Commandes utiles

### Docker

```bash
# Voir tous les conteneurs
docker ps

# Logs d'un service
docker logs -f stockgestion-app
docker logs -f stockgestion-jenkins
docker logs -f stockgestion-sonarqube

# Redémarrer un service
docker restart stockgestion-app

# Arrêter tous les services
docker-compose down

# Nettoyer complètement
docker-compose down -v
docker system prune -a
```

### Maven

```bash
# Build complet
mvn clean package

# Tests uniquement
mvn test

# Skip tests
mvn clean package -DskipTests

# Couverture
mvn clean verify

# SonarQube
mvn sonar:sonar
```

### Application

```bash
# Démarrer l'application
java -jar target/stockgestion-0.0.1-SNAPSHOT.jar

# Avec profil spécifique
java -jar target/stockgestion-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# Avec port différent
java -jar target/stockgestion-0.0.1-SNAPSHOT.jar --server.port=8090
```

---

## 🔧 Configuration initiale recommandée

### 1. SonarQube

```bash
# 1. Se connecter : http://localhost:9000 (admin/admin)
# 2. Changer le mot de passe
# 3. Créer un token :
#    My Account → Security → Generate Tokens
#    Nom : jenkins-token
#    Type : Global Analysis Token
```

### 2. Jenkins - SonarQube Integration

```bash
# 1. Manage Jenkins → Configure System → SonarQube servers
# 2. Add SonarQube :
#    - Name : SonarQube  ⚠️ EXACTEMENT "SonarQube" (ne pas changer!)
#    - Server URL : http://sonarqube:9000
#    - Server authentication token : [ajouter le token créé]
```

> 🚨 **ATTENTION** : Le nom `SonarQube` doit être EXACTEMENT comme indiqué (S majuscule, Q majuscule).
> Ce nom est référencé dans le Jenkinsfile et toute différence causera une erreur de build.

### 3. Jenkins - Maven

```bash
# Manage Jenkins → Global Tool Configuration → Maven
# Add Maven :
#   - Name : Maven
#   - Install automatically : ✓
#   - Version : 3.9.x
```

---

## 📝 Premiers pas avec l'API

### Créer des données de test

```bash
# 1. Créer un client
curl -X POST http://localhost:8080/api/clients \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Client Test",
    "email": "client@test.com",
    "phone": "0612345678"
  }'

# 2. Créer un produit
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop Dell",
    "sku": "LAP-DELL-001",
    "description": "Laptop professionnel",
    "price": 7999.99
  }'

# 3. Consulter l'inventaire
curl http://localhost:8080/api/inventory
```

---

## 🐛 Problèmes fréquents

### Port déjà utilisé

```bash
# Vérifier les ports
sudo netstat -tulpn | grep LISTEN

# Changer le port dans application.properties
server.port=8090

# Ou dans docker-compose.yaml
ports:
  - "8090:8080"
```

### Base de données inaccessible

```bash
# Vérifier PostgreSQL
docker ps | grep postgres

# Redémarrer PostgreSQL
docker restart stockgestion-postgres

# Vérifier les logs
docker logs stockgestion-postgres
```

### Jenkins ne démarre pas

```bash
# Vérifier les logs
docker logs stockgestion-jenkins

# Augmenter la mémoire (dans docker-compose.yaml)
environment:
  JAVA_OPTS: "-Xmx2048m"

# Redémarrer
docker-compose restart jenkins
```

---

## 📚 Prochaines étapes

1. ✅ **Explorer l'API** avec Swagger UI
2. ✅ **Configurer Jenkins** pour CI/CD
3. ✅ **Analyser le code** avec SonarQube
4. ✅ **Lire la documentation** complète
5. ✅ **Contribuer** au projet

---

## 🎯 Checklist de démarrage

- [ ] Docker et Docker Compose installés
- [ ] Services démarrés avec `docker-compose up -d`
- [ ] Application accessible sur http://localhost:8080
- [ ] Swagger UI accessible
- [ ] Jenkins configuré et opérationnel
- [ ] SonarQube configuré
- [ ] Premier build Jenkins réussi
- [ ] Tests unitaires passent
- [ ] API testée avec curl ou Postman

---

## 📞 Besoin d'aide ?

- 📖 **Documentation complète** : [README.md](../Readme.md)
- 🔨 **Guide Jenkins** : [JENKINS_SETUP.md](JENKINS_SETUP.md)
- 📊 **Guide Pipeline** : [PIPELINE.md](PIPELINE.md)
- 🐛 **Issues GitHub** : [Stockflow Issues](https://github.com/Mohamed-Hmidouch/Stockflow/issues)

---

**⏱️ Temps estimé : 5-10 minutes**  
**💡 Astuce : Utilisez le script `cicd-manager.sh` pour gérer facilement tous les services !**

---

**Bon développement ! 🚀**

# 1️⃣ ADMIN
Username: admin
Password: admin123
String: "admin:admin123"
Base64: YWRtaW46YWRtaW4xMjM=
Header: Authorization: Basic YWRtaW46YWRtaW4xMjM=

# 2️⃣ WAREHOUSE_MANAGER  
Username: manager
Password: manager123
String: "manager:manager123"
Base64: bWFuYWdlcjptYW5hZ2VyMTIz
Header: Authorization: Basic bWFuYWdlcjptYW5hZ2VyMTIz

# 3️⃣ CLIENT
Username: client
Password: client123
String: "client:client123"
Base64: Y2xpZW50OmNsaWVudDEyMw==
Header: Authorization: Basic Y2xpZW50OmNsaWVudDEyMw==