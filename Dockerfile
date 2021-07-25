FROM openjdk:8-alpine

COPY target/uberjar/exceed.jar /exceed/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/exceed/app.jar"]
