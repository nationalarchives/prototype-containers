FROM openjdk:8-alpine
WORKDIR /root/test
RUN apk update && apk add curl
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY
ENV AWS_ACCESS_KEY_ID $AWS_ACCESS_KEY_ID
ENV AWS_SECRET_ACCESS_KEY $AWS_SECRET_ACCESS_KEY
COPY target/scala-2.12/tdr-containers-assembly-0.1.0-SNAPSHOT.jar .
CMD java -jar tdr-containers-assembly-0.1.0-SNAPSHOT.jar