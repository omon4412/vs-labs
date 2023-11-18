FROM amazoncorretto:17-alpine-jdk
COPY target/*.jar app.jar
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:7080
ENTRYPOINT ["java","-jar","/app.jar"]