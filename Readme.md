# 🏭 StockGestion - Système de Gestion de Stock

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Coverage](https://img.shields.io/badge/coverage-80%25-green)]()
[![Java](https://img.shields.io/badge/Java-17-orange)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()

Application Spring Boot complète pour la gestion de stock avec pipeline CI/CD intégré.

---

## 📋 Table des matières

- [Fonctionnalités](#-fonctionnalités)
- [Architecture](#-architecture)
- [Technologies](#-technologies)
- [Installation](#-installation)
- [Utilisation](#-utilisation)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Documentation](#-documentation)
- [Développement](#-développement)
- [Tests](#-tests)
- [Contribution](#-contribution)

---

## 🚀 Fonctionnalités

### Gestion complète
- ✅ **Produits** : Création, modification, suppression
- ✅ **Inventaire** : Suivi des stocks en temps réel
- ✅ **Commandes d'achat** : Gestion des approvisionnements
- ✅ **Commandes de vente** : Traitement des ventes
- ✅ **Expéditions** : Suivi des livraisons
- ✅ **Entrepôts** : Multi-entrepôts

### Fonctionnalités techniques
- 🔐 **API RESTful** : Documentation Swagger/OpenAPI
- 📊 **Dashboard** : Métriques et statistiques
- 🔄 **Événements** : Système d'événements pour les workflows
- ⚡ **Performance** : Optimisations JPA et cache
- 🐳 **Containerisation** : Docker et Docker Compose

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (Future)                     │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                  REST API Layer                          │
│    (Controllers + Swagger Documentation)                 │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                  Service Layer                           │
│         (Business Logic + Events)                        │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│              Repository Layer (JPA)                      │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│              PostgreSQL Database                         │
└─────────────────────────────────────────────────────────┘
```

---

## 🛠️ Technologies

### Backend
- **Java 17** - Langage de programmation
- **Spring Boot 3.5.7** - Framework
- **Spring Data JPA** - Persistence
- **Hibernate** - ORM
- **PostgreSQL 16** - Base de données
- **Maven** - Build tool
- **ModelMapper** - Object mapping
- **Lombok** - Réduction du boilerplate

### Documentation & Testing
- **SpringDoc OpenAPI 3** - Documentation API
- **Swagger UI** - Interface interactive
- **JUnit 5** - Tests unitaires
- **JaCoCo** - Couverture de code
- **Mockito** - Mocking

### DevOps & CI/CD
- **Docker** - Containerisation
- **Docker Compose** - Orchestration
- **Jenkins** - CI/CD Pipeline
- **SonarQube** - Qualité du code
- **Git** - Version control

---

## 📦 Installation

### Prérequis

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

### Installation rapide

1. **Cloner le repository**
```bash
git clone https://github.com/Mohamed-Hmidouch/Stockflow.git
cd stockgestion
```

2. **Démarrer l'environnement complet**
```bash
# Option 1 : Script automatique
./cicd-manager.sh
# Choisir l'option 1 (Démarrage complet)

# Option 2 : Docker Compose manuel
docker-compose up -d
```

3. **Build et démarrage (sans Docker)**
```bash
# Build Maven
mvn clean package

# Démarrer PostgreSQL
docker-compose up -d postgres

# Lancer l'application
java -jar target/stockgestion-0.0.1-SNAPSHOT.jar
```

### URLs d'accès

| Service | URL | Credentials |
|---------|-----|-------------|
| Application | http://localhost:8080 | - |
| Swagger UI | http://localhost:8080/swagger-ui.html | - |
| API Docs | http://localhost:8080/api-docs | - |
| Jenkins | http://localhost:8081 | Voir logs |
| SonarQube | http://localhost:9000 | admin/admin |
| PgAdmin | http://localhost:5051 | admin@stockgestion.com/admin123 |
| PostgreSQL | localhost:5433 | stockuser/stockpass |

---

## 💻 Utilisation

### API Endpoints principaux

#### Produits
```bash
# Lister tous les produits
GET /api/products

# Créer un produit
POST /api/products
{
  "name": "Laptop HP",
  "sku": "LAP-HP-001",
  "description": "Laptop professionnel",
  "price": 5999.99
}

# Obtenir un produit
GET /api/products/{id}

# Mettre à jour un produit
PUT /api/products/{id}

# Supprimer un produit
DELETE /api/products/{id}
```

#### Inventaire
```bash
# Consulter l'inventaire
GET /api/inventory

# Vérifier le stock d'un produit
GET /api/inventory/product/{productId}
```

#### Commandes de vente
```bash
# Créer une commande
POST /api/sales-orders
{
  "clientId": 1,
  "orderDate": "2025-11-12T14:00:00",
  "orderLines": [
    {
      "productId": 1,
      "quantity": 5,
      "unitPrice": 5999.99
    }
  ]
}

# Lister les commandes
GET /api/sales-orders

# Obtenir une commande
GET /api/sales-orders/{id}
```

Pour plus de détails, consultez la **Swagger UI** : http://localhost:8080/swagger-ui.html

---

## 🔄 CI/CD Pipeline

### Architecture du pipeline

```
GitHub → Jenkins → Build → Tests → SonarQube → Docker → Deploy
```

### Configuration Jenkins

Consultez la documentation complète : [JENKINS_SETUP.md](docs/JENKINS_SETUP.md)

**Quick Start** :
```bash
# 1. Démarrer Jenkins
docker-compose up -d jenkins

# 2. Récupérer le mot de passe initial
docker exec stockgestion-jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# 3. Accéder à Jenkins
# http://localhost:8081

# 4. Créer un pipeline
# Pointer vers le Jenkinsfile dans le repository
```

### Jenkinsfile stages

1. 📦 **Checkout** - Récupération du code
2. 🧹 **Clean** - Nettoyage
3. 🔍 **Compile** - Compilation
4. 🧪 **Tests Unitaires** - Exécution des tests
5. 📦 **Package** - Création du JAR
6. 📊 **JaCoCo** - Couverture de code
7. 🔍 **SonarQube** - Analyse qualité
8. 🚦 **Quality Gate** - Vérification seuils
9. 🐳 **Docker Build** - Construction image
10. 🚀 **Deploy** - Déploiement

Consultez : [PIPELINE.md](docs/PIPELINE.md)

---

## 📚 Documentation

### Documentation disponible

- 📘 [Configuration Jenkins](docs/JENKINS_SETUP.md) - Guide complet Jenkins
- 📗 [Pipeline CI/CD](docs/PIPELINE.md) - Documentation du pipeline
- 📙 [SonarQube Quality Gates](docs/SONARQUBE_QUALITY_GATES.md) - Critères de qualité
- 📕 [API Documentation](http://localhost:8080/swagger-ui.html) - Swagger UI

### Structure du projet

```
stockgestion/
├── src/
│   ├── main/
│   │   ├── java/com/example/stockgestion/
│   │   │   ├── config/           # Configuration Spring
│   │   │   ├── controlleurs/     # REST Controllers
│   │   │   ├── services/         # Business Logic
│   │   │   ├── repositories/     # JPA Repositories
│   │   │   ├── models/           # Entités JPA
│   │   │   ├── Dto/              # Data Transfer Objects
│   │   │   ├── events/           # Événements système
│   │   │   └── exception/        # Gestion des exceptions
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/                 # Tests unitaires
├── docs/                         # Documentation
├── target/                       # Build output
├── Dockerfile                    # Configuration Docker
├── compose.yaml                  # Docker Compose
├── Jenkinsfile                   # Pipeline CI/CD
├── cicd-manager.sh              # Script de gestion
└── pom.xml                       # Configuration Maven
```

---

## 👨‍💻 Développement

### Workflow Git

```bash
# Avant chaque user story, mettre à jour main
git checkout main
git pull origin main

# Créer une branche pour la user story
git checkout -b SS-XX-description-feature

# Développer et committer
git add .
git commit -m "SS-XX: Description du changement"
git push -u origin SS-XX-description-feature

# Créer une Pull Request sur GitHub
# Après merge, revenir à main
git checkout main
git pull origin main
```

### Convention de nommage des branches

- `SS-XX-*` : User stories (ex: SS-25-configuration-added-for-ci-cd)
- `bugfix/*` : Corrections de bugs
- `feature/*` : Nouvelles fonctionnalités
- `hotfix/*` : Corrections urgentes

### Code Style

- **Java** : Suivre les conventions Java standard
- **Spring Boot** : Bonnes pratiques Spring
- **Tests** : Minimum 80% de couverture
- **Documentation** : Javadoc pour les méthodes publiques

---

## 🧪 Tests

### Exécuter les tests

```bash
# Tests unitaires
mvn test

# Tests + couverture
mvn clean verify

# Rapport de couverture
mvn jacoco:report
# Ouvrir target/site/jacoco/index.html
```

### Tests actuels

- ✅ **31 tests** unitaires
- ✅ **0 failures**
- ✅ **46 classes** analysées
- 📊 Couverture en cours d'amélioration

### Structure des tests

```
src/test/java/
└── com/example/stockgestion/
    ├── services/
    │   ├── SalesOrderServiceCreateOrderTest.java
    │   ├── SalesOrderServiceCancelOrderTest.java
    │   ├── SalesOrderServiceShipOrderTest.java
    │   └── SalesOrderServiceStatusTransitionTest.java
    └── StockGerationApplicationTests.java
```

---

## 🤝 Contribution

### Comment contribuer

1. **Fork** le projet
2. Créer une **branche feature** (`git checkout -b feature/AmazingFeature`)
3. **Commit** vos changements (`git commit -m 'Add AmazingFeature'`)
4. **Push** vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une **Pull Request**

### Checklist avant PR

- [ ] Tests unitaires passent (`mvn test`)
- [ ] Couverture > 80% pour le nouveau code
- [ ] Build Maven réussit (`mvn clean package`)
- [ ] Pas d'erreur SonarQube critique
- [ ] Code formaté correctement
- [ ] Documentation mise à jour
- [ ] Commit messages descriptifs

---

## 🔧 Configuration

### Variables d'environnement

```properties
# Base de données
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/stockgestiondb
SPRING_DATASOURCE_USERNAME=stockuser
SPRING_DATASOURCE_PASSWORD=stockpass

# JPA
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true

# Application
SPRING_APPLICATION_NAME=StockGeration
SERVER_PORT=8080
```

### Profils Spring

```bash
# Développement
java -jar app.jar --spring.profiles.active=dev

# Production
java -jar app.jar --spring.profiles.active=prod

# Tests
mvn test -Dspring.profiles.active=test
```

---

## 📊 Métriques du projet

### Code
- **Lignes de code** : ~5000+ lignes
- **Classes** : 92 classes Java
- **Couverture** : 80%+ (objectif)

### Tests
- **Tests unitaires** : 31
- **Tests d'intégration** : En cours
- **Taux de réussite** : 100%

### Performance
- **Build time** : ~26 secondes
- **Startup time** : ~8 secondes
- **Image Docker** : ~250 MB

---

## 🐛 Troubleshooting

### Problèmes courants

**Application ne démarre pas**
```bash
# Vérifier PostgreSQL
docker ps | grep postgres

# Voir les logs
docker logs stockgestion-app
```

**Tests échouent**
```bash
# Nettoyer et rebuild
mvn clean install

# Vérifier la base de données
docker exec -it stockgestion-postgres psql -U stockuser -d stockgestiondb
```

**Docker build échoue**
```bash
# Rebuild sans cache
docker build --no-cache -t stockgestion-app .

# Vérifier le JAR
ls -lh target/*.jar
```

---

## 📄 License

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

---

## 👥 Équipe

**StockGestion Development Team**
- Mohamed Hmidouch - [@Mohamed-Hmidouch](https://github.com/Mohamed-Hmidouch)

---

## 📞 Support

- 📧 Email: support@stockgestion.com
- 🐛 Issues: [GitHub Issues](https://github.com/Mohamed-Hmidouch/Stockflow/issues)
- 📖 Wiki: [GitHub Wiki](https://github.com/Mohamed-Hmidouch/Stockflow/wiki)

---

## 🗺️ Roadmap

### Version 1.0 (Actuelle)
- [x] API RESTful complète
- [x] Gestion des stocks
- [x] Pipeline CI/CD
- [x] Documentation Swagger
- [x] Tests unitaires

### Version 1.1 (Prochaine)
- [ ] Tests d'intégration
- [ ] Performance optimization
- [ ] Authentification JWT
- [ ] Frontend React

### Version 2.0 (Future)
- [ ] Microservices architecture
- [ ] Kafka event streaming
- [ ] Monitoring (Prometheus/Grafana)
- [ ] Mobile app

---

**📝 Dernière mise à jour** : 12 Novembre 2025  
**🔖 Version** : 0.0.1-SNAPSHOT  
**⭐ N'oubliez pas de mettre une étoile si ce projet vous aide !**
