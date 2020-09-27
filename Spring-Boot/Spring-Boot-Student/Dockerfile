FROM openjdk:8-jre-alpine
COPY target/*.jar /app.jar
EXPOSE 33001
ENV spring.profiles.active=prod
CMD /usr/bin/java -jar /app.jar