FROM openjdk:18
RUN mkdir /opt/app
COPY target/socialnetwork-0.0.1-SNAPSHOT.jar /opt/app/socialnetwork.jar
CMD ["java", "-jar", "/opt/app/socialnetwork.jar"]
