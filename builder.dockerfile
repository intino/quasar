FROM openjdk:21-jdk-slim
LABEL maintainer="octavio.roncal <octavioroncal@siani.es>"
LABEL version="1.0.0"
LABEL description="Quassar builder"
LABEL operations="Build"
LABEL targets="Java"
ARG MAVEN_VERSION=3.9.9
ARG MAVEN_HOME=/usr/share/maven
ARG MAVEN_REPOSITORY=/app/.m2/repository
ENV MAVEN_HOME=${MAVEN_HOME}
ENV PATH=${MAVEN_HOME}/bin:${PATH}
RUN apt-get update && \
    apt-get install -y curl tar
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz -o /tmp/maven.tar.gz
RUN mkdir -p ${MAVEN_HOME}
RUN tar -xzf /tmp/maven.tar.gz -C ${MAVEN_HOME} --strip-components=1 && \
    rm -f /tmp/maven.tar.gz && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
COPY docker/dependencies-pom.xml /app/dependencies-pom.xml
RUN mkdir -p /app/.m2/repository && \
    mvn -f /app/dependencies-pom.xml dependency:go-offline -Dmaven.repo.local=${MAVEN_REPOSITORY}
COPY out/build/quassar-builder/builder.jar /app/
COPY out/build/quassar-builder/dependency /app/dependency
COPY docker/run-builder.sh /app/run-builder.sh
RUN chmod a+rx /app/run-builder.sh
WORKDIR /app
ENV MAVEN_CONFIG=/app/.m2
ENTRYPOINT ["/app/run-builder.sh"]