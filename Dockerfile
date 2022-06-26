FROM openjdk:11
COPY ./target/*.jar /app.jar
CMD  echo "wait elasticsearch" ; sleep 10s ; java -jar /app.jar
