# Configuration SonarQube Quality Gates pour StockGestion

## 🎯 Objectifs de Qualité

Ce fichier définit les critères de qualité minimaux pour le projet StockGestion.

---

## 📊 Métriques SonarQube

### 🔴 Conditions obligatoires (Quality Gate)

| Métrique | Opérateur | Seuil | Description |
|----------|-----------|-------|-------------|
| **Coverage** | < | 80% | Couverture de code minimale |
| **Duplicated Lines** | > | 3% | Taux de duplication maximal |
| **Maintainability Rating** | > | A | Note de maintenabilité |
| **Reliability Rating** | > | A | Note de fiabilité |
| **Security Rating** | > | A | Note de sécurité |
| **Security Hotspots Reviewed** | < | 100% | Hotspots de sécurité revus |
| **Blocker Issues** | > | 0 | Aucun problème bloquant |
| **Critical Issues** | > | 0 | Aucun problème critique |

### 📈 Métriques de surveillance (Non bloquantes)

| Métrique | Valeur cible | Description |
|----------|--------------|-------------|
| **Code Smells** | < 50 | Problèmes de qualité mineurs |
| **Technical Debt** | < 5 jours | Dette technique |
| **Cognitive Complexity** | < 15 par méthode | Complexité cognitive |
| **Cyclomatic Complexity** | < 10 par méthode | Complexité cyclomatique |

---

## 🛠️ Configuration dans SonarQube

### 1. Créer un Quality Gate personnalisé

1. Connectez-vous à SonarQube : http://localhost:9000
2. Allez dans **Quality Gates**
3. Cliquez sur **Create**
4. Nom : `StockGestion Quality Gate`

### 2. Ajouter les conditions

#### Coverage
- Condition : On Overall Code
- Metric : Coverage
- Operator : is less than
- Value : 80

#### Duplications
- Condition : On Overall Code
- Metric : Duplicated Lines (%)
- Operator : is greater than
- Value : 3

#### Maintainability
- Condition : On Overall Code
- Metric : Maintainability Rating
- Operator : is worse than
- Value : A

#### Reliability
- Condition : On Overall Code
- Metric : Reliability Rating
- Operator : is worse than
- Value : A

#### Security
- Condition : On Overall Code
- Metric : Security Rating
- Operator : is worse than
- Value : A

#### Blocker Issues
- Condition : On Overall Code
- Metric : Blocker Issues
- Operator : is greater than
- Value : 0

#### Critical Issues
- Condition : On Overall Code
- Metric : Critical Issues
- Operator : is greater than
- Value : 0

### 3. Associer au projet

1. Dans **Quality Gates**, sélectionnez `StockGestion Quality Gate`
2. Dans **Projects**, cliquez sur le projet `stockgestion`
3. Cliquez sur **Set as Default** ou associez manuellement

---

## 📋 Exclusions SonarQube

### Fichiers exclus de l'analyse

Configuré dans `pom.xml` :

```xml
<sonar.exclusions>
    **/config/**,          <!-- Configuration classes -->
    **/Dto/**,             <!-- DTOs -->
    **/models/**,          <!-- Modèles JPA -->
    **/exception/**        <!-- Classes d'exception -->
</sonar.exclusions>
```

### Raisons des exclusions

- **Config** : Configuration Spring, pas de logique métier
- **DTO** : Classes de transfert de données, getters/setters générés
- **Models** : Entités JPA avec annotations, pas de logique complexe
- **Exception** : Classes d'exception simples

---

## 🔍 Règles personnalisées (optionnel)

### Règles Java activées

| Règle | Sévérité | Description |
|-------|----------|-------------|
| **S1192** | Major | Éviter les String literals dupliqués |
| **S1075** | Major | Éviter les chemins hardcodés |
| **S106** | Major | Éviter System.out.println |
| **S2095** | Blocker | Fermer les ressources |
| **S2259** | Blocker | Éviter les NullPointerException |
| **S1144** | Major | Supprimer les méthodes inutilisées |
| **S1172** | Major | Supprimer les paramètres inutilisés |

### Règles Spring Boot spécifiques

| Règle | Sévérité | Description |
|-------|----------|-------------|
| **S3749** | Major | @SpringBootApplication au bon endroit |
| **S4684** | Major | Utiliser @Transactional correctement |
| **S3655** | Major | Utiliser Optional correctement |

---

## 📊 Configuration via API SonarQube

### Script de configuration automatique

```bash
#!/bin/bash

SONAR_URL="http://localhost:9000"
SONAR_TOKEN="your-token-here"
PROJECT_KEY="stockgestion"

# Créer le Quality Gate
curl -u "${SONAR_TOKEN}:" -X POST \
  "${SONAR_URL}/api/qualitygates/create" \
  -d "name=StockGestion Quality Gate"

# Obtenir l'ID du Quality Gate
QG_ID=$(curl -u "${SONAR_TOKEN}:" -s \
  "${SONAR_URL}/api/qualitygates/list" | \
  jq -r '.qualitygates[] | select(.name=="StockGestion Quality Gate") | .id')

# Ajouter les conditions
curl -u "${SONAR_TOKEN}:" -X POST \
  "${SONAR_URL}/api/qualitygates/create_condition" \
  -d "gateId=${QG_ID}" \
  -d "metric=coverage" \
  -d "op=LT" \
  -d "error=80"

curl -u "${SONAR_TOKEN}:" -X POST \
  "${SONAR_URL}/api/qualitygates/create_condition" \
  -d "gateId=${QG_ID}" \
  -d "metric=duplicated_lines_density" \
  -d "op=GT" \
  -d "error=3"

# Plus de conditions...

# Associer au projet
curl -u "${SONAR_TOKEN}:" -X POST \
  "${SONAR_URL}/api/qualitygates/select" \
  -d "gateId=${QG_ID}" \
  -d "projectKey=${PROJECT_KEY}"

echo "Quality Gate configured successfully!"
```

---

## 🎯 Objectifs par type de code

### Controllers (Controlleurs)
- Coverage : **> 90%**
- Complexity : **< 5 par méthode**
- Raison : Logique simple, facile à tester

### Services
- Coverage : **> 85%**
- Complexity : **< 15 par méthode**
- Raison : Logique métier importante

### Repositories
- Coverage : **> 70%** (tests d'intégration)
- Raison : Tests avec base de données

### DTOs / Models
- **Exclus** de l'analyse
- Raison : Pas de logique métier

---

## 📈 Suivi des métriques

### Dashboard SonarQube

Accédez à : **http://localhost:9000/dashboard?id=stockgestion**

Métriques visibles :
- 🐛 Bugs
- 🔒 Vulnerabilities
- 🔥 Code Smells
- 📊 Coverage
- 📋 Duplications
- 💰 Technical Debt

### Tendances

SonarQube suit l'évolution des métriques :
- **New Code** : Nouveau code depuis la dernière version
- **Overall Code** : Code complet du projet
- **Activity** : Historique des analyses

---

## 🚨 Actions en cas d'échec du Quality Gate

### Si le Quality Gate échoue :

1. **Consulter le rapport SonarQube**
   - Aller sur http://localhost:9000
   - Consulter les issues détectées

2. **Corriger les problèmes par priorité**
   - Blocker issues → en premier
   - Critical issues → ensuite
   - Major issues → puis

3. **Re-tester localement**
   ```bash
   mvn clean verify sonar:sonar
   ```

4. **Commit et push**
   - Le pipeline Jenkins re-vérifiera automatiquement

---

## 📝 Bonnes pratiques

### Avant de committer

```bash
# 1. Exécuter les tests
mvn clean test

# 2. Vérifier la couverture
mvn jacoco:report
# Ouvrir target/site/jacoco/index.html

# 3. Analyse SonarQube locale
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=your-token

# 4. Vérifier le rapport
# Ouvrir http://localhost:9000/dashboard?id=stockgestion
```

### Pendant le développement

- ✅ Écrire les tests en même temps que le code
- ✅ Viser **> 80% de couverture** pour le nouveau code
- ✅ Refactorer les méthodes complexes (complexity > 15)
- ✅ Éviter les duplications de code
- ✅ Documenter les méthodes publiques (Javadoc)

---

## 🔧 Configuration recommandée par IDE

### IntelliJ IDEA

1. Installer le plugin **SonarLint**
2. Lier au serveur SonarQube local
3. Activer l'analyse en temps réel

### VS Code

1. Installer l'extension **SonarLint**
2. Configurer dans `settings.json` :
```json
{
  "sonarlint.connectedMode.servers": [{
    "serverId": "sonarqube-local",
    "serverUrl": "http://localhost:9000",
    "token": "your-token"
  }]
}
```

---

## 📚 Ressources

- [SonarQube Documentation](https://docs.sonarqube.org/)
- [Quality Gates](https://docs.sonarqube.org/latest/user-guide/quality-gates/)
- [Metric Definitions](https://docs.sonarqube.org/latest/user-guide/metric-definitions/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)

---

**📝 Configuration actuelle** : StockGestion v0.0.1-SNAPSHOT  
**📅 Dernière mise à jour** : 12 Novembre 2025  
**👤 Équipe** : StockGestion Development Team
