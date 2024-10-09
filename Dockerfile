FROM gradle:8.0.0-jdk19

WORKDIR /opt/app
COPY ./build/libs/TorqueHub-0.0.1-SNAPSHOT.jar ./

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar TorqueHub-0.0.1-SNAPSHOT.jar"]