FROM java:8
ADD build/libs/fint-consumer-organisasjon-*.jar /data/app.jar
CMD ["java", "-jar", "/data/app.jar"]