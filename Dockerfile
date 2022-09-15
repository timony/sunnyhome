FROM arm64v8/amazoncorretto:18-al2-jdk
ENV TZ="Europe/Prague"
COPY target/SunnyHome-0.5.jar /SunnyHome-0.5.jar
COPY data/sample_sunny.properties /data/sample_sunny.properties
CMD ["java", "-jar", "/SunnyHome-0.5.jar"]