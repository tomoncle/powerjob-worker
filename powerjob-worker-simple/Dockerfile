FROM tomoncle/java:oracle-1.8.0
MAINTAINER tomoncle <tomoncle@sina.com>

ENV BIND_PORT=8082
ENV WORK_PORT=27778
ENV APP_NAME=powerjob-worker-simple
ENV SERVER_ADDR=127.0.0.1:7700
ENV BIND_EXT_IP=""
ENV BIND_IP=""
# app start args
ENV JVM_OPS=""
ENV ARGS=""

COPY target/powerjob-worker-simple.jar /opt/app.jar
COPY src/main/resources                /opt/config

EXPOSE 8082

CMD ["java","${JVM_OPS}", "-jar", "/opt/app.jar","${ARGS}"]
