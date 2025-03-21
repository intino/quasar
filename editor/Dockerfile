FROM openjdk:21-jdk-slim

WORKDIR /app

COPY out/build/editor/dependency dependency
COPY out/build/editor/editor.jar app.jar
COPY docker/archetype ./data

ENV HOME_DIR=./data
ENV PORT=8080
ENV TITLE=Quassar
ENV FEDERATION_URL=
ENV LANGUAGE_ARTIFACTORY=https://artifactory.intino.io/artifactory/releases

EXPOSE ${PORT}

COPY editor/entrypoint.sh ./entrypoint.sh
RUN chmod +x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]