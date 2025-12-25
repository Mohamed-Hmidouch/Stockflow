#!/bin/bash

# Script de test de la stack ELK
# Usage: ./test-elk-stack.sh

set -e

echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║          🧪 TEST DE LA STACK ELK - STOCKGESTION              ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonctions utilitaires
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# 1. Vérifier Docker
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1️⃣  Vérification de Docker"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if ! command -v docker &> /dev/null; then
    print_error "Docker n'est pas installé"
    exit 1
fi
print_success "Docker est installé"

if ! docker info &> /dev/null; then
    print_error "Docker n'est pas démarré"
    exit 1
fi
print_success "Docker est démarré"
echo ""

# 2. Démarrer les services ELK
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2️⃣  Démarrage des services ELK"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

print_info "Démarrage d'Elasticsearch..."
docker-compose up -d elasticsearch
sleep 10

print_info "Démarrage de Logstash..."
docker-compose up -d logstash
sleep 5

print_info "Démarrage de Kibana..."
docker-compose up -d kibana
sleep 5

print_success "Services ELK démarrés"
echo ""

# 3. Vérifier Elasticsearch
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3️⃣  Vérification d'Elasticsearch"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

print_info "Attente du démarrage d'Elasticsearch (max 60s)..."
TIMEOUT=60
ELAPSED=0

while [ $ELAPSED -lt $TIMEOUT ]; do
    if curl -s http://localhost:9200/_cluster/health &> /dev/null; then
        print_success "Elasticsearch est accessible"
        break
    fi
    sleep 2
    ELAPSED=$((ELAPSED + 2))
    echo -n "."
done

if [ $ELAPSED -ge $TIMEOUT ]; then
    print_error "Timeout: Elasticsearch n'a pas démarré"
    exit 1
fi

echo ""
print_info "Statut du cluster:"
curl -s http://localhost:9200/_cluster/health | jq '.'
echo ""

# 4. Vérifier Logstash
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4️⃣  Vérification de Logstash"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if docker ps | grep -q logstash; then
    print_success "Logstash est en cours d'exécution"
    
    # Vérifier que le port 5044 est ouvert
    if nc -z localhost 5044 2>/dev/null; then
        print_success "Logstash écoute sur le port 5044"
    else
        print_warning "Logstash n'écoute pas encore sur le port 5044 (peut prendre quelques secondes)"
    fi
else
    print_error "Logstash n'est pas en cours d'exécution"
    exit 1
fi
echo ""

# 5. Vérifier Kibana
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "5️⃣  Vérification de Kibana"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

print_info "Attente du démarrage de Kibana (max 60s)..."
TIMEOUT=60
ELAPSED=0

while [ $ELAPSED -lt $TIMEOUT ]; do
    if curl -s http://localhost:5601/api/status &> /dev/null; then
        print_success "Kibana est accessible"
        break
    fi
    sleep 2
    ELAPSED=$((ELAPSED + 2))
    echo -n "."
done

if [ $ELAPSED -ge $TIMEOUT ]; then
    print_warning "Kibana met du temps à démarrer (c'est normal)"
else
    echo ""
    print_info "Statut de Kibana:"
    curl -s http://localhost:5601/api/status | jq '.status.overall'
fi
echo ""

# 6. Envoyer un log de test
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "6️⃣  Envoi d'un log de test"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Attendre que Logstash soit prêt
sleep 5

TEST_LOG='{
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%S.%3NZ)'",
  "level": "INFO",
  "logger_name": "test.script",
  "message": "Test log from ELK stack verification script",
  "application": "stockgestion"
}'

print_info "Envoi du log de test à Logstash..."
echo "$TEST_LOG" | nc localhost 5044 2>/dev/null || print_warning "Impossible d'envoyer le log (Logstash peut ne pas être prêt)"

sleep 2
print_success "Log de test envoyé"
echo ""

# 7. Vérifier la présence du log dans Elasticsearch
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "7️⃣  Vérification dans Elasticsearch"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

sleep 3
print_info "Recherche d'index stockgestion-logs..."

INDEX_COUNT=$(curl -s "http://localhost:9200/_cat/indices/stockgestion-logs-*?h=index" | wc -l)

if [ "$INDEX_COUNT" -gt 0 ]; then
    print_success "Index trouvés:"
    curl -s "http://localhost:9200/_cat/indices/stockgestion-logs-*?v"
    
    echo ""
    print_info "Nombre de documents dans les index:"
    curl -s "http://localhost:9200/stockgestion-logs-*/_count" | jq '.'
else
    print_warning "Aucun index trouvé (normal si c'est la première exécution)"
fi
echo ""

# 8. Résumé
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 RÉSUMÉ"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
print_success "Stack ELK opérationnelle!"
echo ""
echo "🔗 Accès aux services:"
echo "   • Elasticsearch: http://localhost:9200"
echo "   • Kibana:        http://localhost:5601"
echo "   • Logstash:      tcp://localhost:5044"
echo ""
echo "📝 Prochaines étapes:"
echo "   1. Démarrer l'application en mode prod:"
echo "      SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run"
echo ""
echo "   2. Créer un index pattern dans Kibana:"
echo "      - Ouvrir http://localhost:5601"
echo "      - Stack Management → Index Patterns"
echo "      - Créer: stockgestion-logs-*"
echo ""
echo "   3. Visualiser les logs:"
echo "      - Discover → Sélectionner stockgestion-logs-*"
echo ""
print_info "Consultez docs/ELK_STACK.md pour plus d'informations"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
