FROM openjdk:21-jdk-slim

WORKDIR /root/app

COPY out/build/editor/dependency dependency
COPY out/build/editor/editor.jar app.jar

ENV HOME_DIR=./data
ENV PORT=8080
ENV TITLE=Quassar
ENV FEDERATION_URL=
ENV LANGUAGE_ARTIFACTORY=https://artifactory.intino.io/artifactory/releases

EXPOSE ${PORT}

COPY docker/run-editor.sh ./run-editor.sh
RUN chmod +x run-editor.sh

ENTRYPOINT ["./run-editor.sh"]