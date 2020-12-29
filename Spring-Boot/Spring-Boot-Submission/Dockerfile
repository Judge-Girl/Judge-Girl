FROM adoptopenjdk/openjdk11:alpine-slim
RUN apk add build-base
COPY target/*.jar /app.jar
EXPOSE 80
CMD /opt/java/openjdk/bin/java -jar /app.jar