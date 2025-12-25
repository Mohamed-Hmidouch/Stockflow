# 📊 STACK ELK (Elasticsearch, Logstash, Kibana)

## 📋 Vue d'ensemble

La stack ELK a été ajoutée au projet pour centraliser et visualiser les logs de l'application StockGestion.

### 🔧 Composants

1. **Elasticsearch** (Port 9200)
   - Base de données NoSQL pour stocker les logs
   - Indexation et recherche rapide
   - Stockage: volume Docker `es_data`

2. **Logstash** (Port 5044)
   - Pipeline de traitement des logs
   - Réception depuis l'application (TCP JSON)
   - Envoi vers Elasticsearch

3. **Kibana** (Port 5601)
   - Interface web de visualisation
   - Création de dashboards
   - Analyse des logs en temps réel

---

## 🚀 Démarrage

### Démarrer toute la stack

```bash
docker-compose up -d elasticsearch logstash kibana
```

### Vérifier le statut

```bash
# Elasticsearch
curl http://localhost:9200/_cluster/health

# Kibana
curl http://localhost:5601/api/status

# Voir les logs
docker-compose logs -f logstash
```

### Démarrer l'application avec ELK

```bash
# En mode production (logs envoyés à Logstash)
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run

# En mode dev (logs console uniquement)
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

---

## 📊 Accès aux interfaces

| Service | URL | Description |
|---------|-----|-------------|
| Kibana | http://localhost:5601 | Interface de visualisation |
| Elasticsearch | http://localhost:9200 | API REST Elasticsearch |
| Logstash | tcp://localhost:5044 | Endpoint TCP pour logs |

---

## 🔍 Configuration Kibana

### 1. Créer un Index Pattern

1. Ouvrir Kibana: http://localhost:5601
2. Aller dans **Stack Management** → **Index Patterns**
3. Créer un pattern: `stockgestion-logs-*`
4. Choisir `@timestamp` comme champ de temps

### 2. Visualiser les logs

1. Aller dans **Discover**
2. Sélectionner l'index pattern `stockgestion-logs-*`
3. Voir les logs en temps réel

### 3. Créer des dashboards

Exemples de visualisations utiles:
- **Logs par niveau** (INFO, WARN, ERROR)
- **Timeline des erreurs**
- **Logs par service/classe**
- **Requêtes HTTP les plus fréquentes**

---

## 📝 Configuration de l'application

### Fichiers modifiés

1. **compose.yaml**
   - Ajout des services Elasticsearch, Logstash, Kibana
   - Configuration réseau et volumes

2. **logstash.conf**
   - Pipeline de traitement des logs
   - Input: TCP port 5044 (JSON)
   - Output: Elasticsearch

3. **pom.xml**
   - Dépendance: `logstash-logback-encoder`

4. **logback-spring.xml**
   - Appender LOGSTASH pour envoyer les logs
   - Profils: dev (console) vs prod (console + Logstash + fichier)

---

## 🎯 Utilisation

### Exemple de logging dans le code

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    
    public Product createProduct(ProductDto dto) {
        log.info("Création d'un produit: {}", dto.getSku());
        
        try {
            Product product = productRepository.save(product);
            log.info("Produit créé avec succès: ID={}", product.getId());
            return product;
        } catch (Exception e) {
            log.error("Erreur lors de la création du produit", e);
            throw e;
        }
    }
}
```

### Requêtes Kibana utiles

```
# Tous les logs ERROR
level:ERROR

# Logs d'un service spécifique
logger_name:"com.example.stockgestion.services.ProductService"

# Logs avec exception
_exists_:stack_trace

# Logs des dernières 15 minutes
@timestamp:[now-15m TO now]
```

---

## 🛠️ Maintenance

### Nettoyer les anciens logs

```bash
# Supprimer les index de plus de 30 jours
curl -X DELETE "localhost:9200/stockgestion-logs-$(date -d '30 days ago' +%Y.%m.%d)"
```

### Arrêter la stack ELK

```bash
docker-compose stop elasticsearch logstash kibana
```

### Supprimer les données Elasticsearch

```bash
docker-compose down -v  # Supprime tous les volumes
# OU
docker volume rm stockgestion_es_data
```

---

## 🔒 Sécurité (Production)

⚠️ **IMPORTANT**: La configuration actuelle désactive la sécurité Elasticsearch pour simplifier le développement.

Pour la production, activez la sécurité:

```yaml
# compose.yaml - Elasticsearch
environment:
  - xpack.security.enabled=true
  - ELASTIC_PASSWORD=votre_mot_de_passe_fort
```

Et configurez l'authentification dans Logstash et Kibana.

---

## 📈 Optimisation des performances

### Elasticsearch

- **Mémoire JVM**: Ajustez `ES_JAVA_OPTS` selon vos besoins (actuellement 1GB)
- **Nombre de shards**: Par défaut, 1 shard par index (suffisant pour dev)
- **Index Lifecycle**: Configurez la rotation automatique des index

### Logstash

- **Mémoire JVM**: Actuellement 512MB, ajustable via `LS_JAVA_OPTS`
- **Workers**: Ajoutez `pipeline.workers` dans logstash.conf si nécessaire

---

## 🐛 Dépannage

### Elasticsearch ne démarre pas

```bash
# Vérifier les logs
docker-compose logs elasticsearch

# Problème de mémoire ? Augmenter vm.max_map_count
sudo sysctl -w vm.max_map_count=262144
```

### L'application n'envoie pas de logs

1. Vérifier le profil actif: `SPRING_PROFILES_ACTIVE=prod`
2. Vérifier que Logstash est démarré
3. Vérifier les logs de l'application: `logs/stockgestion.log`

### Kibana n'affiche pas les logs

1. Vérifier que l'index pattern existe
2. Vérifier la période de temps sélectionnée
3. Vérifier qu'Elasticsearch contient des données:
   ```bash
   curl http://localhost:9200/stockgestion-logs-*/_count
   ```

---

## 📚 Ressources

- [Elasticsearch Documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Logstash Documentation](https://www.elastic.co/guide/en/logstash/current/index.html)
- [Kibana Documentation](https://www.elastic.co/guide/en/kibana/current/index.html)
- [Logstash Logback Encoder](https://github.com/logfellow/logstash-logback-encoder)

---

## ✅ Checklist de vérification

- [ ] Elasticsearch accessible sur port 9200
- [ ] Logstash accessible sur port 5044
- [ ] Kibana accessible sur port 5601
- [ ] Index pattern créé dans Kibana
- [ ] Application en mode `prod` envoie les logs
- [ ] Logs visibles dans Kibana Discover
- [ ] Dashboard créé (optionnel)
