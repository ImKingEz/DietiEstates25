# Usa l'immagine base di Amazon Corretto JDK 21 Alpine
FROM amazoncorretto:21-alpine-jdk

# Definisci la directory di lavoro nel container
WORKDIR /app

# Copia il file JAR della tua applicazione (assicurati che il path corrisponda)
COPY target/*.jar app.jar

# Espone la porta 8080
EXPOSE 8080

# Definisci il comando per eseguire l'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]