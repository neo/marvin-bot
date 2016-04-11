FROM pivotalsg/marvin-base:1.0.1

RUN mkdir -p /opt/dependencies/
COPY build.gradle gradle* /opt/dependencies/
WORKDIR /opt/dependencies
RUN mkdir gradle && mv wrapper gradle/ && sleep 1
RUN chmod +x gradlew && ./gradlew resolveAllDependencies

ENTRYPOINT ./gradlew
