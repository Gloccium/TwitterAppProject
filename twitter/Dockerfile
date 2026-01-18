FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 9001

ENTRYPOINT ["java","-jar","/app/app.jar"]
