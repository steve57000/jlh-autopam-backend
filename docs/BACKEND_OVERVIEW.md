# JLH AutoPam Backend – Guide technique

## 1. Vue d'ensemble
JLH AutoPam est une API Spring Boot 3.5 visant à gérer les clients, demandes de services, rendez-vous et promotions d'un garage automobile. Le projet s'appuie sur Java 17, Spring Boot, Spring Security, Spring Data JPA, validation Jakarta, envoi d'e-mails et un stockage MySQL, avec MapStruct et Lombok pour la génération de code, ainsi que des bibliothèques JWT pour l'authentification stateless.【F:pom.xml†L1-L135】 L'application principale active la planification de tâches et la lecture de propriétés typées pour le garage via `@EnableScheduling` et `@EnableConfigurationProperties`.【F:src/main/java/com/jlh/jlhautopambackend/JlhApplication.java†L1-L20】

## 2. Outils de build et conteneurisation
- **Maven** compile et empaquette l'application (plugin `spring-boot-maven-plugin`) et configure les processeurs d'annotation MapStruct/Lombok via `maven-compiler-plugin`.【F:pom.xml†L137-L210】
- **Docker** propose une image multi-étapes : compilation dans un conteneur Maven puis exécution dans un JRE léger avec profil `prod` et un healthcheck HTTP.【F:Dockerfile†L1-L28】
- **Compose développement** : lance MySQL 8, MailHog, le backend en profil `dev` avec volumes montés pour le code, le dépôt Maven et le volume partagé `/var/www/promo`, ainsi qu'un Nginx frontal.【F:docker-compose.dev.yml†L1-L81】
- **Compose production** : démarre MySQL, le backend packagé, et un Nginx qui partage le volume `promo-images` pour servir les visuels des promotions.【F:docker-compose.prod.yml†L1-L57】
- **Documentation Docker** : `docs/DOCKER.md` détaille les commandes pour nettoyer, lancer et mettre à jour les environnements, ainsi que la configuration Nginx de référence.【F:docs/DOCKER.md†L1-L179】

## 3. Configuration et profils
- **Configuration générique (`application.yml`)** : propriétés métier (nom/adresse du garage, fuseau horaire), URLs front/back, paramètres d'e-mail, répertoire d'upload `/var/www/promo` et taille maximale des fichiers, ainsi que la clé et la durée des JWT.【F:src/main/resources/application.yml†L1-L26】
- **Profil développement (`application-dev.properties`)** : connexion MySQL locale paramétrable par variables, dialecte MySQL 8, initialisation automatique `schema.sql` + `data.sql`, même dossier d'upload que Nginx, URL publique des images et configuration MailHog.【F:src/main/resources/application-dev.properties†L1-L35】
- **Profil test (`application-test.properties`)** : utilise H2 en mémoire, regénère le schéma à chaque test et désactive le chargement des scripts SQL, avec Mockito configuré pour l'inline mocking.【F:src/test/resources/application-test.properties†L1-L11】【F:src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker†L1-L1】
- **Propriétés spécialisées** : `GarageProperties` fournit un binding typé pour les informations du garage consommées ailleurs dans l'application.【F:src/main/java/com/jlh/jlhautopambackend/JlhApplication.java†L1-L12】

## 4. Arborescence du code source
- `src/main/java/com/jlh/jlhautopambackend`
  - **`config`** : sécurité (`SecurityConfig`, `JwtAuthenticationFilter`) et binding de configuration (`GarageProperties`).【F:src/main/java/com/jlh/jlhautopambackend/config/SecurityConfig.java†L1-L136】【F:src/main/java/com/jlh/jlhautopambackend/config/JwtAuthenticationFilter.java†L1-L54】
  - **`controllers`** : expose les APIs REST pour l'authentification, clients, services, demandes, rendez-vous, promotions, etc.【81a09d†L1-L5】
  - **`advice` & `web`** : deux `@RestControllerAdvice` centralisent la gestion des erreurs de validation, conflits ou exceptions serveur (`ValidationAdvice`, `RestExceptionHandler`).【F:src/main/java/com/jlh/jlhautopambackend/advice/ValidationAdvice.java†L1-L28】【F:src/main/java/com/jlh/jlhautopambackend/web/RestExceptionHandler.java†L1-L88】
  - **`dto`** : payloads d'entrée/sortie pour séparer API et entités JPA.【1e96ae†L1-L9】
  - **`mapper`** : interfaces MapStruct pour convertir entités ↔ DTO.【08f7f1†L1-L4】
  - **`modeles`** : entités JPA (clients, demandes, promotions, tokens, etc.).【fbac19†L1-L5】
  - **`repository`** : interfaces Spring Data pour les accès base.【9ad635†L1-L7】
  - **`services`** : logique métier (gestion des demandes, promotions, stockage de fichiers, mailing, tokens, planification). Sous-paquet `support` pour utilitaires liés au contexte de sécurité.【f91f04†L1-L10】【F:src/main/java/com/jlh/jlhautopambackend/services/support/AuthenticatedClientResolver.java†L1-L27】
  - **`utils`** : utilitaires transverses (`JwtUtil`, génération ICS).【F:src/main/java/com/jlh/jlhautopambackend/utils/JwtUtil.java†L1-L87】
- `src/main/resources`
  - Configurations de profils, scripts SQL d'initialisation (`schema_jlh_autopam.sql`, `data.sql`) et éventuels gabarits HTML.【F:src/main/resources/application.yml†L1-L26】【F:src/main/resources/schema_jlh_autopam.sql†L1-L88】【F:src/main/resources/data.sql†L1-L120】
- `nginx/` : configuration du proxy statique et reverse-proxy pour servir `/promotions/images/` depuis le volume partagé.【F:nginx/nginx.conf†L1-L22】【F:nginx/conf.d/promotions.conf†L1-L29】
- `docs/` : documentation complémentaire (Docker et ce guide).
- `src/test/java` : tests unitaires/intégration couvrant contrôleurs, services, DTO et mappers.【eaa762†L1-L6】【bb2e54†L1-L6】

## 5. Sécurité et authentification
- **Filtrage JWT** : `JwtAuthenticationFilter` valide les tokens portés par l'en-tête `Authorization` et installe les autorités dans le `SecurityContextHolder`.【F:src/main/java/com/jlh/jlhautopambackend/config/JwtAuthenticationFilter.java†L1-L54】
- **Utilitaire JWT** : `JwtUtil` lit la clé Base64, génère des tokens contenant les rôles et vérifie signature/expiration.【F:src/main/java/com/jlh/jlhautopambackend/utils/JwtUtil.java†L20-L87】
- **Configuration HTTP** : `SecurityConfig` désactive CSRF (API stateless), force les sessions sans état, décrit les routes publiques/clients/admin et configure CORS explicite vers le front Angular, tout en personnalisant les réponses 401/403 JSON.【F:src/main/java/com/jlh/jlhautopambackend/config/SecurityConfig.java†L39-L135】
- **Contrôleur d'authentification** : gère login JWT, inscription client avec envoi de mail, renvoi/validation du lien de vérification et réinitialisation de mot de passe, en s'appuyant sur `ClientService`, `EmailVerificationService` et `PasswordResetService`.【F:src/main/java/com/jlh/jlhautopambackend/controllers/AuthController.java†L32-L146】

## 6. Gestion des promotions et stockage de fichiers
- **Stockage disque** : `FileSystemStorageService` s'appuie sur `app.upload-dir`, crée le dossier au démarrage, génère un nom unique (UUID + nom nettoyé) et enregistre/supprime les fichiers correspondants.【F:src/main/java/com/jlh/jlhautopambackend/services/FileSystemStorageService.java†L15-L43】
- **Service métier** : `PromotionServiceImpl` valide la période, lie l'administrateur porteur, stocke/remplace les images et nettoie l'ancienne ressource lors des mises à jour/suppressions tout en exposant des méthodes DTOisées pour les contrôleurs et les tests.【F:src/main/java/com/jlh/jlhautopambackend/services/PromotionServiceImpl.java†L44-L176】
- **Nettoyage planifié** : `PromotionCleanupService` s'exécute chaque jour à 23h59, sélectionne les promotions expirées (`validTo` avant le prochain minuit), supprime les fichiers associés si présents puis efface les enregistrements en base, avec logs détaillés en cas d'erreur disque.【F:src/main/java/com/jlh/jlhautopambackend/services/PromotionCleanupService.java†L38-L99】
- **Nginx** : le virtual host `promotions.conf` publie `/promotions/images/` depuis `/var/www/promo` (volume partagé avec le backend) et reverse-proxy le reste vers Spring Boot.【F:nginx/conf.d/promotions.conf†L1-L29】

## 7. Flux clients/demandes/rendez-vous
- **Entités et relations** : `schema_jlh_autopam.sql` définit clients, services, demandes, statuts, créneaux, rendez-vous et promotions, avec les tables de jointure nécessaires pour les disponibilités et les demandes/services.【F:src/main/resources/schema_jlh_autopam.sql†L1-L88】
- **Données de démonstration** : `data.sql` alimente lookups, services, comptes tests et exemples de demandes/rendez-vous pour faciliter les tests manuels et front-end.【F:src/main/resources/data.sql†L1-L120】
- **Services applicatifs** : le package `services` expose des interfaces/implémentations pour orchestrer la création de demandes, leur conversion en devis ou rendez-vous, la réservation de créneaux et la génération d'e-mails. Les contrôleurs correspondants orchestrent ces cas d'usage via DTO et mappers.【f91f04†L1-L10】【81a09d†L1-L5】
- **Résolution du client courant** : `AuthenticatedClientResolver` fournit un accès centralisé au `Client` issu du token pour mutualiser la logique côté contrôleurs client.【F:src/main/java/com/jlh/jlhautopambackend/services/support/AuthenticatedClientResolver.java†L9-L26】

## 8. Notifications et e-mails
- **Service d'envoi** : `MailService` encapsule `JavaMailSender`, alimente les expéditeurs depuis la configuration et envoie du HTML (développement via MailHog).【F:src/main/java/com/jlh/jlhautopambackend/services/MailService.java†L1-L26】
- **Vérification e-mail** : `EmailVerificationService` génère un token aléatoire, purge les anciens, stocke l'expiration puis envoie un lien basé sur `app.baseUrl`; validation du lien marque le client comme vérifié et consomme le token.【F:src/main/java/com/jlh/jlhautopambackend/services/EmailVerificationService.java†L16-L58】
- **Réinitialisation mot de passe** : `PasswordResetServiceImpl` crée des tokens à durée limitée, notifie le front via `app.frontendUrl`, sécurise l'encodage du mot de passe et invalide le token après usage.【F:src/main/java/com/jlh/jlhautopambackend/services/PasswordResetServiceImpl.java†L19-L68】

## 9. Couche de présentation REST
- **Contrôleurs métier** : `PromotionController` illustre les endpoints multipart pour créer/mettre à jour les promotions avec image, en renvoyant des DTO et des statuts HTTP adaptés.【F:src/main/java/com/jlh/jlhautopambackend/controllers/PromotionController.java†L17-L61】 De nombreux autres contrôleurs suivent le même schéma pour administrateurs, clients, demandes, rendez-vous et référentiels.【81a09d†L1-L5】
- **Gestion des erreurs** : `ValidationAdvice` et `RestExceptionHandler` assurent des réponses JSON cohérentes pour les erreurs de validation, conflits de données, ressources manquantes et exceptions inattendues, facilitant la consommation front-end.【F:src/main/java/com/jlh/jlhautopambackend/advice/ValidationAdvice.java†L9-L27】【F:src/main/java/com/jlh/jlhautopambackend/web/RestExceptionHandler.java†L31-L88】

## 10. Tests
- **Organisation** : les tests Java sont regroupés par couche (`controllers`, `services`, `dto`, `mapper`) et réutilisent la configuration H2 pour des scénarios rapides.【bb2e54†L1-L6】【eaa762†L1-L6】
- **Jeu de données** : les scripts `schema_jlh_autopam.sql` et `data.sql` servent de base pour les tests manuels, tandis que la configuration `application-test.properties` évite de polluer la base MySQL locale durant les tests automatisés.【F:src/main/resources/schema_jlh_autopam.sql†L1-L88】【F:src/main/resources/data.sql†L1-L120】【F:src/test/resources/application-test.properties†L1-L11】

## 11. Références supplémentaires
- **Infrastructure Nginx** : `nginx/conf.d/promotions.conf` et `nginx/nginx.conf` servent de base de configuration dans les environnements containerisés et doivent rester synchronisés avec `app.upload-dir` côté Spring Boot.【F:nginx/nginx.conf†L1-L22】【F:nginx/conf.d/promotions.conf†L1-L29】
- **Volume des images** : le répertoire `/var/www/promo` doit être partagé entre le backend et Nginx (via Docker ou configuration serveur) pour garantir la disponibilité des images de promotions créées par l'API.【F:docker-compose.dev.yml†L34-L76】【F:docker-compose.prod.yml†L20-L47】【F:src/main/resources/application.yml†L7-L13】

Ce document complète la documentation Docker existante et sert de guide d'accueil pour les nouveaux développeurs backend ou intégrateurs DevOps travaillant sur JLH AutoPam.
