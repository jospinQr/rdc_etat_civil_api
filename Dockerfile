
FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp

# Installe 'netcat-openbsd' et 'dos2unix' sans cache
# 'dos2unix' est ajouté pour convertir les fins de ligne si le script vient de Windows
RUN apk add --no-cache netcat-openbsd dos2unix
WORKDIR /app
COPY build/libs/rdc_etat_civil-0.0.1-SNAPSHOT.jar app.jar

# Copie le script wait-for-mysql.sh dans le conteneur.
# Il est copié dans /app/wait-for-mysql.sh.
COPY wait-for-mysql.sh wait-for-mysql.sh

# Convertit les fins de ligne du script wait-for-mysql.sh en format Unix (LF)
# Ceci résout le problème "no such file or directory" provenant des scripts Windows (CRLF).
RUN dos2unix wait-for-mysql.sh

# Rend le script exécutable
RUN chmod +x wait-for-mysql.sh

EXPOSE 8080
# Définit le point d'entrée pour exécuter le script, puis l'application Java
# L'utilisation de 'exec' est préférable pour la gestion des signaux par Docker.
ENTRYPOINT ["./wait-for-mysql.sh"]