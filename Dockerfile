# ======================================
# Dockerfile pour StockGestion Spring Boot
# ======================================

# Étape 1 : choisir une image JDK légère
FROM eclipse-temurin:17-jdk-jammy

# Étape 2 : copier le jar construit depuis target
COPY target/stockgestion-0.0.1-SNAPSHOT.jar app.jar

# Étape 3 : exposer le port utilisé par Spring Boot
EXPOSE 8080

# Étape 4 : commande pour lancer l'application
ENTRYPOINT ["java", "-jar", "/app.jar"]
