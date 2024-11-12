# This Docker file defines a production container for the Resolver Web Service
FROM amazoncorretto:17-alpine
LABEL maintainer="Renato Caminha Juacaba Neto <rjuacaba@ebi.ac.uk>"

# Environment - defaults
ENV WS_SPARQL_JVM_MEMORY_MAX 1024m

# Prepare the application folder
RUN mkdir -p /home/app

# Add the application structure
COPY "./target/sparql-2.1.jar" /home/app/service.jar

# Launch information
EXPOSE 8080
WORKDIR /home/app
#CMD ["java", "-Xmx1024m", "-jar", "service.jar"]
CMD java -Xmx${WS_SPARQL_JVM_MEMORY_MAX} -jar service.jar
