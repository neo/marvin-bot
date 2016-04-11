FROM pivotalsg/marvin-base:1.0.1

RUN mkdir -p /opt/dependencies/
COPY build.gradle gradle* /opt/dependencies/
WORKDIR /opt/dependencies
RUN mkdir gradle && mv wrapper gradle/
RUN sleep 2 && chmod +x gradlew && sleep 2 && ./gradlew resolveAllDependencies

ENTRYPOINT ./gradlew
