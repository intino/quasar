FROM openjdk:21-jdk-slim
LABEL maintainer="octavio.roncal <octavioroncal@siani.es>"
LABEL version="1.0.0"
LABEL description="Quassar builder"
LABEL operations="Build"
LABEL targets="Java"
ARG MAVEN_VERSION=3.9.9
ARG MAVEN_HOME=/usr/share/maven
ENV MAVEN_HOME=${MAVEN_HOME}
ENV PATH=${MAVEN_HOME}/bin:${PATH}
RUN apt-get update && \
    apt-get install -y curl tar
RUN curl -fsSL https://downloads.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz -o /tmp/maven.tar.gz
RUN mkdir -p ${MAVEN_HOME}
RUN tar -xzf /tmp/maven.tar.gz -C ${MAVEN_HOME} --strip-components=1 && \
    rm -f /tmp/maven.tar.gz && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
COPY out/build/quassar-builder/builder.jar /root/app/
COPY out/build/quassar-builder/dependency /root/app/dependency
COPY docker/run-builder.sh /root/app/
WORKDIR /root/app
RUN chmod +x /root/app/run-builder.sh
ENTRYPOINT ["/root/app/run-builder.sh"]