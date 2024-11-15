FROM openjdk:17-jdk-alpine

COPY target/petBuddy-backend-0.0.1-SNAPSHOT.jar petBuddy-backend-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/petBuddy-backend-0.0.1-SNAPSHOT.jar"]