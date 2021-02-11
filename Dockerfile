FROM openjdk:8-jre-alpine

COPY out/artifacts/TAStreamer_jar/TAStreamer.jar /TAStreamer.jar

CMD ["java", "-jar", "/TAStreamer.jar"]