#!/bin/bash

# ============================================
# 🚀 Script de démarrage CI/CD StockGestion
# ============================================

set -e  # Arrêter en cas d'erreur

# Couleurs pour les logs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonctions d'affichage
print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Vérifier que Docker est installé
check_docker() {
    print_info "Vérification de Docker..."
    if ! command -v docker &> /dev/null; then
        print_error "Docker n'est pas installé. Veuillez l'installer d'abord."
        exit 1
    fi
    print_success "Docker est installé ($(docker --version))"
}

# Vérifier que Docker Compose est installé
check_docker_compose() {
    print_info "Vérification de Docker Compose..."
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose n'est pas installé. Veuillez l'installer d'abord."
        exit 1
    fi
    print_success "Docker Compose est installé ($(docker-compose --version))"
}

# Vérifier que Maven est installé
check_maven() {
    print_info "Vérification de Maven..."
    if ! command -v mvn &> /dev/null; then
        print_warning "Maven n'est pas installé localement. Jenkins utilisera sa propre version."
    else
        print_success "Maven est installé ($(mvn --version | head -n 1))"
    fi
}

# Construire l'application
build_app() {
    print_header "📦 Construction de l'application"
    print_info "Nettoyage et build Maven..."
    mvn clean package -DskipTests
    print_success "Application construite avec succès"
}

# Démarrer PostgreSQL
start_postgres() {
    print_header "🐘 Démarrage de PostgreSQL"
    docker-compose up -d postgres
    
    print_info "Attente du démarrage de PostgreSQL..."
    sleep 5
    
    # Vérifier que PostgreSQL est prêt
    until docker exec stockgestion-postgres pg_isready -U stockuser -d stockgestiondb > /dev/null 2>&1; do
        echo -n "."
        sleep 1
    done
    echo ""
    print_success "PostgreSQL est prêt"
}

# Démarrer SonarQube
start_sonarqube() {
    print_header "📊 Démarrage de SonarQube"
    docker-compose up -d sonarqube
    
    print_info "Attente du démarrage de SonarQube (cela peut prendre 1-2 minutes)..."
    
    # Attendre que SonarQube soit prêt
    max_attempts=60
    attempt=0
    while [ $attempt -lt $max_attempts ]; do
        if curl -s http://localhost:9000/api/system/status | grep -q '"status":"UP"'; then
            echo ""
            print_success "SonarQube est prêt"
            print_info "SonarQube UI: http://localhost:9000 (admin/admin)"
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo ""
    print_warning "SonarQube prend plus de temps que prévu, mais continue en arrière-plan..."
}

# Démarrer Jenkins
start_jenkins() {
    print_header "🔨 Démarrage de Jenkins"
    docker-compose up -d jenkins
    
    print_info "Attente du démarrage de Jenkins (cela peut prendre 1-2 minutes)..."
    
    # Attendre que Jenkins soit prêt
    max_attempts=60
    attempt=0
    while [ $attempt -lt $max_attempts ]; do
        if curl -s http://localhost:8081/login > /dev/null 2>&1; then
            echo ""
            print_success "Jenkins est prêt"
            
            # Récupérer le mot de passe initial
            if docker exec stockgestion-jenkins test -f /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null; then
                JENKINS_PASSWORD=$(docker exec stockgestion-jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null)
                print_info "Jenkins UI: http://localhost:8081"
                print_info "Mot de passe initial: ${JENKINS_PASSWORD}"
            else
                print_info "Jenkins UI: http://localhost:8081"
                print_info "Jenkins est déjà configuré"
            fi
            return 0
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo ""
    print_warning "Jenkins prend plus de temps que prévu, mais continue en arrière-plan..."
}

# Démarrer l'application Spring Boot
start_app() {
    print_header "🚀 Démarrage de l'application StockGestion"
    docker-compose up -d stockgestion-app
    
    print_info "Attente du démarrage de l'application..."
    sleep 10
    
    if docker ps | grep -q stockgestion-app; then
        print_success "Application démarrée"
        print_info "Application UI: http://localhost:8080"
        print_info "Swagger UI: http://localhost:8080/swagger-ui.html"
        print_info "API Docs: http://localhost:8080/api-docs"
    else
        print_error "L'application n'a pas démarré correctement"
        print_info "Vérifiez les logs: docker logs stockgestion-app"
    fi
}

# Démarrer PgAdmin
start_pgadmin() {
    print_header "🗄️  Démarrage de PgAdmin"
    docker-compose up -d pgadmin
    
    print_info "Attente du démarrage de PgAdmin..."
    sleep 5
    
    print_success "PgAdmin est prêt"
    print_info "PgAdmin UI: http://localhost:5051 (admin@stockgestion.com/admin123)"
}

# Afficher le statut de tous les services
show_status() {
    print_header "📊 Statut des services"
    docker-compose ps
    
    echo ""
    print_header "🔗 URLs d'accès"
    echo -e "${BLUE}Application:${NC}    http://localhost:8080"
    echo -e "${BLUE}Swagger UI:${NC}     http://localhost:8080/swagger-ui.html"
    echo -e "${BLUE}Jenkins:${NC}        http://localhost:8081"
    echo -e "${BLUE}SonarQube:${NC}      http://localhost:9000"
    echo -e "${BLUE}PgAdmin:${NC}        http://localhost:5051"
    echo -e "${BLUE}PostgreSQL:${NC}     localhost:5433"
}

# Afficher les logs
show_logs() {
    print_header "📋 Logs des services"
    echo "1. Application (stockgestion-app)"
    echo "2. Jenkins"
    echo "3. SonarQube"
    echo "4. PostgreSQL"
    echo "5. Tous les services"
    read -p "Choisissez un service (1-5): " choice
    
    case $choice in
        1) docker logs -f stockgestion-app ;;
        2) docker logs -f stockgestion-jenkins ;;
        3) docker logs -f stockgestion-sonarqube ;;
        4) docker logs -f stockgestion-postgres ;;
        5) docker-compose logs -f ;;
        *) print_error "Choix invalide" ;;
    esac
}

# Arrêter tous les services
stop_all() {
    print_header "🛑 Arrêt de tous les services"
    docker-compose down
    print_success "Tous les services sont arrêtés"
}

# Redémarrer tous les services
restart_all() {
    print_header "🔄 Redémarrage de tous les services"
    docker-compose restart
    print_success "Tous les services sont redémarrés"
}

# Nettoyer complètement l'environnement
clean_all() {
    print_header "🧹 Nettoyage complet"
    print_warning "Cela supprimera tous les conteneurs, volumes et images"
    read -p "Êtes-vous sûr ? (y/N): " confirm
    
    if [[ $confirm == [yY] ]]; then
        docker-compose down -v
        docker rmi stockgestion-app:latest 2>/dev/null || true
        print_success "Nettoyage terminé"
    else
        print_info "Nettoyage annulé"
    fi
}

# Menu principal
show_menu() {
    echo ""
    print_header "🚀 StockGestion CI/CD Manager"
    echo "1.  🚀 Démarrage complet (All services)"
    echo "2.  🐘 Démarrer PostgreSQL uniquement"
    echo "3.  📊 Démarrer SonarQube uniquement"
    echo "4.  🔨 Démarrer Jenkins uniquement"
    echo "5.  🌐 Démarrer l'application uniquement"
    echo "6.  📊 Voir le statut des services"
    echo "7.  📋 Voir les logs"
    echo "8.  🔄 Redémarrer tous les services"
    echo "9.  🛑 Arrêter tous les services"
    echo "10. 🧹 Nettoyage complet"
    echo "11. 📦 Build Maven (local)"
    echo "0.  ❌ Quitter"
    echo ""
    read -p "Choisissez une option: " option
    
    case $option in
        1)
            check_docker
            check_docker_compose
            check_maven
            start_postgres
            start_sonarqube
            start_jenkins
            start_pgadmin
            start_app
            show_status
            ;;
        2)
            check_docker
            check_docker_compose
            start_postgres
            ;;
        3)
            check_docker
            check_docker_compose
            start_sonarqube
            ;;
        4)
            check_docker
            check_docker_compose
            start_jenkins
            ;;
        5)
            check_docker
            check_docker_compose
            start_app
            ;;
        6)
            show_status
            ;;
        7)
            show_logs
            ;;
        8)
            restart_all
            ;;
        9)
            stop_all
            ;;
        10)
            clean_all
            ;;
        11)
            check_maven
            build_app
            ;;
        0)
            print_info "Au revoir!"
            exit 0
            ;;
        *)
            print_error "Option invalide"
            ;;
    esac
    
    # Retourner au menu
    read -p "Appuyez sur Entrée pour continuer..."
    show_menu
}

# Point d'entrée
main() {
    clear
    show_menu
}

# Lancer le script
main
