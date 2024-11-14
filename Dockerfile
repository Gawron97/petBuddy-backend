FROM openjdk:17-jdk-alpine

COPY petBuddy-backend-0.0.1-SNAPSHOT.jar petBuddy-backend.jar
ENTRYPOINT ["java","-jar","/petBuddy-backend.jar"]