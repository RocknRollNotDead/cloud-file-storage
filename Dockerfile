
FROM gradle:9.0-jdk21 AS build
WORKDIR /project

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon
COPY src ./src
RUN gradle bootJar --no-daemon -x test


FROM eclipse-temurin:21-jre
COPY --from=build /project/target/*.jar /usr/local/tomcat/webapps/app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]