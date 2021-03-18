FROM adoptopenjdk/openjdk11:alpine-slim
COPY target/*.jar /app.jar
EXPOSE 80
CMD /opt/java/openjdk/bin/java -jar /app.jar