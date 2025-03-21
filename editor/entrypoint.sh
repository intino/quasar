#!/bin/sh

java --add-opens=java.base/sun.security.action=ALL-UNNAMED -cp dependency/*:app.jar io.quassar.editor.box.Main \
  port="${PORT}" \
  home="${HOME_DIR}" \
  title="${TITLE}" \
  federation-url="${FEDERATION_URL}" \
  language-artifactory="${LANGUAGE_ARTIFACTORY}"