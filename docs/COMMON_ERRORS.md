# 🐛 Erreurs courantes Jenkins CI/CD - StockGestion

Guide de résolution rapide des erreurs fréquentes lors de l'utilisation du pipeline Jenkins.

---

## 🚨 Erreurs critiques

### ❌ Erreur #1 : "No SonarQube server configured with name 'SonarQube'"

**Message complet** :
```
ERROR: No SonarQube server configured with name 'SonarQube'
```

**Cause** :
Le nom du serveur SonarQube dans Jenkins ne correspond pas au nom utilisé dans le Jenkinsfile.

**Solution étape par étape** :

1. **Aller dans Jenkins** : http://localhost:8081

2. **Naviguer vers la configuration** :
   - Cliquer sur "Manage Jenkins" (Gérer Jenkins)
   - Cliquer sur "Configure System" (Configurer le système)
   - Descendre jusqu'à la section "SonarQube servers"

3. **Vérifier le nom** :
   - Le champ **"Name"** DOIT être exactement : `SonarQube`
   - ✅ Correct : `SonarQube` (S majuscule, Q majuscule)
   - ❌ Incorrect : `sonarqube`, `SonarQube-Server`, `Mon-SonarQube`, `SONARQUBE`

4. **Corriger si nécessaire** :
   - Modifier le nom pour qu'il soit exactement `SonarQube`
   - Cliquer sur "Save" (Sauvegarder)

5. **Relancer le build** :
   - Retourner au job Jenkins
   - Cliquer sur "Build Now"

**Ligne concernée dans le Jenkinsfile** :
```groovy
// Ligne ~125
withSonarQubeEnv('SonarQube') {  // <-- Le nom ici doit matcher
```

---

### ❌ Erreur #2 : "mvn: command not found"

**Message complet** :
```
/bin/sh: 1: mvn: not found
```

**Cause** :
Maven n'est pas configuré dans Jenkins.

**Solution** :

1. **Aller dans Jenkins** : Manage Jenkins → Global Tool Configuration

2. **Configurer Maven** :
   - Section "Maven"
   - Cliquer "Add Maven"
   - Name : `Maven` (exactement comme dans le Jenkinsfile)
   - Cocher "Install automatically"
   - Version : Choisir 3.9.x ou supérieur
   - Sauvegarder

3. **Alternative - Installation manuelle dans le conteneur** :
```bash
docker exec -u root stockgestion-jenkins apt-get update
docker exec -u root stockgestion-jenkins apt-get install -y maven
docker restart stockgestion-jenkins
```

---

### ❌ Erreur #3 : "permission denied while trying to connect to Docker daemon socket"

**Message complet** :
```
Got permission denied while trying to connect to the Docker daemon socket
```

**Cause** :
Le conteneur Jenkins n'a pas les permissions pour accéder au socket Docker.

**Solution** :

```bash
# Donner les permissions au socket Docker
docker exec -u root stockgestion-jenkins chmod 666 /var/run/docker.sock

# Ou redémarrer le conteneur
docker restart stockgestion-jenkins
```

**Solution permanente - Modifier compose.yaml** :
```yaml
jenkins:
  user: root  # Ajouter cette ligne
  volumes:
    - /var/run/docker.sock:/var/run/docker.sock
```

---

### ❌ Erreur #4 : "Connection refused: sonarqube:9000"

**Message complet** :
```
java.net.ConnectException: Connection refused (Connection refused)
```

**Cause** :
SonarQube n'est pas démarré ou pas accessible.

**Solution** :

1. **Vérifier que SonarQube est démarré** :
```bash
docker ps | grep sonarqube
```

2. **Démarrer SonarQube si nécessaire** :
```bash
docker-compose up -d sonarqube
```

3. **Attendre que SonarQube soit prêt** (1-2 minutes) :
```bash
# Vérifier les logs
docker logs -f stockgestion-sonarqube

# Attendre le message "SonarQube is up"
```

4. **Tester l'accès** :
```bash
curl http://localhost:9000/api/system/status
# Doit retourner : {"status":"UP"}
```

5. **Vérifier la connectivité depuis Jenkins** :
```bash
docker exec stockgestion-jenkins curl http://sonarqube:9000/api/system/status
```

---

### ❌ Erreur #5 : "Quality Gate timeout"

**Message complet** :
```
Timeout after 5 minutes while waiting for quality gate
```

**Cause** :
SonarQube prend trop de temps pour analyser le code.

**Solution** :

1. **Augmenter le timeout dans le Jenkinsfile** :
```groovy
// Ligne ~136
timeout(time: 10, unit: 'MINUTES') {  // Augmenter de 5 à 10 minutes
    script {
        def qg = waitForQualityGate()
```

2. **Vérifier que l'analyse SonarQube s'est bien terminée** :
   - Aller sur http://localhost:9000
   - Vérifier le projet `stockgestion`
   - L'analyse doit être "Complete"

3. **Vérifier les ressources du conteneur SonarQube** :
```bash
docker stats stockgestion-sonarqube
```

---

## ⚠️ Erreurs non-critiques

### ⚠️ Avertissement #1 : "Spring Data JDBC - Could not safely identify store assignment"

**Message** :
```
Spring Data JDBC - Could not safely identify store assignment for repository candidate
```

**Cause** :
Spring Data essaie de détecter si les repositories sont JPA ou JDBC.

**Impact** :
Aucun - C'est juste un avertissement informatif. Les repositories JPA fonctionnent correctement.

**Solution** :
Vous pouvez ignorer cet avertissement ou ajouter `@Repository` sur vos interfaces de repository.

---

### ⚠️ Avertissement #2 : "spring.jpa.open-in-view is enabled by default"

**Message** :
```
spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering.
```

**Cause** :
Configuration par défaut de Spring Boot.

**Impact** :
Performance potentielle dans certains cas d'utilisation.

**Solution (optionnelle)** :
Ajouter dans `application.properties` :
```properties
spring.jpa.open-in-view=false
```

---

## 📦 Erreurs de build Maven

### ❌ Erreur #6 : "Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin"

**Message** :
```
There are test failures.
```

**Cause** :
Un ou plusieurs tests unitaires échouent.

**Solution** :

1. **Voir les détails des tests** :
```bash
# Localement
mvn test

# Voir les rapports
cat target/surefire-reports/*.txt
```

2. **Dans Jenkins** :
   - Cliquer sur le build
   - Aller dans "Test Results"
   - Voir les tests qui échouent

3. **Corriger les tests** et recommitter

4. **Skip temporairement les tests (déconseillé)** :
```bash
mvn clean package -DskipTests
```

---

### ❌ Erreur #7 : "Could not resolve dependencies"

**Message** :
```
Could not resolve dependencies for project com.example:stockgestion
```

**Cause** :
Problème de connexion au repository Maven Central ou dépendance introuvable.

**Solution** :

1. **Vérifier la connexion Internet** :
```bash
curl https://repo.maven.apache.org/maven2/
```

2. **Nettoyer le cache Maven** :
```bash
rm -rf ~/.m2/repository
mvn clean install
```

3. **Vérifier le pom.xml** :
   - Pas de dépendances avec des versions inexistantes
   - Syntaxe XML correcte

---

## 🐳 Erreurs Docker

### ❌ Erreur #8 : "Cannot connect to the Docker daemon"

**Message** :
```
Cannot connect to the Docker daemon at unix:///var/run/docker.sock
```

**Cause** :
Docker n'est pas démarré ou le socket n'est pas monté.

**Solution** :

1. **Vérifier que Docker est démarré** :
```bash
sudo systemctl status docker
sudo systemctl start docker
```

2. **Vérifier le montage du socket** :
```bash
docker exec stockgestion-jenkins ls -l /var/run/docker.sock
```

3. **Si le socket n'existe pas**, vérifier `compose.yaml` :
```yaml
volumes:
  - /var/run/docker.sock:/var/run/docker.sock
```

---

### ❌ Erreur #9 : "Error response from daemon: Conflict"

**Message** :
```
Error response from daemon: Conflict. The container name "/stockgestion-app" is already in use
```

**Cause** :
Un conteneur avec le même nom existe déjà.

**Solution** :

```bash
# Arrêter et supprimer le conteneur existant
docker stop stockgestion-app
docker rm stockgestion-app

# Ou avec Docker Compose
docker-compose down
docker-compose up -d
```

---

## 🗄️ Erreurs PostgreSQL

### ❌ Erreur #10 : "Connection refused: postgres"

**Message** :
```
org.postgresql.util.PSQLException: Connection refused
```

**Cause** :
PostgreSQL n'est pas démarré ou pas accessible.

**Solution** :

1. **Vérifier PostgreSQL** :
```bash
docker ps | grep postgres
```

2. **Démarrer PostgreSQL** :
```bash
docker-compose up -d postgres
```

3. **Tester la connexion** :
```bash
docker exec -it stockgestion-postgres psql -U stockuser -d stockgestiondb -c "SELECT 1;"
```

4. **Vérifier les credentials** dans `application.properties` :
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/stockgestiondb
spring.datasource.username=stockuser
spring.datasource.password=stockpass
```

---

## 📊 Tableau récapitulatif des erreurs

| # | Erreur | Gravité | Solution rapide |
|---|--------|---------|-----------------|
| 1 | SonarQube server not found | 🔴 Critique | Vérifier nom = "SonarQube" |
| 2 | mvn not found | 🔴 Critique | Configurer Maven dans Jenkins |
| 3 | Docker permission denied | 🔴 Critique | `chmod 666 /var/run/docker.sock` |
| 4 | Connection refused SonarQube | 🔴 Critique | Démarrer SonarQube |
| 5 | Quality Gate timeout | 🟡 Moyenne | Augmenter timeout |
| 6 | Test failures | 🟡 Moyenne | Corriger les tests |
| 7 | Maven dependencies | 🟡 Moyenne | Nettoyer cache Maven |
| 8 | Docker daemon | 🔴 Critique | Démarrer Docker |
| 9 | Container conflict | 🟢 Faible | Supprimer conteneur |
| 10 | PostgreSQL refused | 🟡 Moyenne | Démarrer PostgreSQL |

---

## 🔍 Commandes de diagnostic

### Vérifier l'état des services

```bash
# Tous les conteneurs
docker ps -a

# Services Docker Compose
docker-compose ps

# Logs d'un service
docker logs -f stockgestion-jenkins
docker logs -f stockgestion-sonarqube
docker logs -f stockgestion-postgres
docker logs -f stockgestion-app
```

### Vérifier la configuration Jenkins

```bash
# Entrer dans le conteneur Jenkins
docker exec -it stockgestion-jenkins bash

# Vérifier Maven
mvn --version

# Vérifier Docker
docker --version

# Vérifier la connectivité SonarQube
curl http://sonarqube:9000/api/system/status
```

### Vérifier SonarQube

```bash
# API Status
curl http://localhost:9000/api/system/status

# Lister les projets
curl -u admin:votre-mot-de-passe http://localhost:9000/api/projects/search

# Vérifier les Quality Gates
curl http://localhost:9000/api/qualitygates/list
```

---

## 📞 Besoin d'aide ?

Si l'erreur persiste après avoir suivi ce guide :

1. **Consulter les logs détaillés** :
   ```bash
   ./cicd-manager.sh  # Option 7 - Voir les logs
   ```

2. **Vérifier la documentation** :
   - [JENKINS_SETUP.md](JENKINS_SETUP.md)
   - [PIPELINE.md](PIPELINE.md)
   - [QUICKSTART.md](QUICKSTART.md)

3. **Créer une issue GitHub** :
   - https://github.com/Mohamed-Hmidouch/Stockflow/issues

---

## ✅ Checklist de dépannage

Avant de demander de l'aide, vérifier :

- [ ] Tous les services sont démarrés (`docker ps`)
- [ ] Jenkins accessible (http://localhost:8081)
- [ ] SonarQube accessible (http://localhost:9000)
- [ ] PostgreSQL démarré et accessible
- [ ] Maven configuré dans Jenkins
- [ ] Nom SonarQube = "SonarQube" exactement
- [ ] Token SonarQube configuré dans Jenkins
- [ ] Socket Docker monté et permissions OK
- [ ] Build Maven local fonctionne (`mvn clean verify`)

---

**📝 Dernière mise à jour** : 12 Novembre 2025  
**🔖 Version** : 1.0.0  
**💡 Astuce** : Gardez ce fichier à portée de main pendant la configuration !
