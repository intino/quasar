/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2024 TypeFox and others.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */
import * as monaco from 'monaco-editor';
import { initServices } from 'monaco-languageclient/vscode/services';
import getThemeServiceOverride from '@codingame/monaco-vscode-theme-service-override';
import getTextmateServiceOverride from '@codingame/monaco-vscode-textmate-service-override';
import '@codingame/monaco-vscode-theme-defaults-default-extension';
import '@codingame/monaco-vscode-json-default-extension';
import '@codingame/monaco-vscode-java-default-extension';
import '@codingame/monaco-vscode-javascript-default-extension';
import { MonacoLanguageClient } from 'monaco-languageclient';
import { WebSocketMessageReader, WebSocketMessageWriter, toSocket } from 'vscode-ws-jsonrpc';
import { CloseAction, ErrorAction } from 'vscode-languageclient';
import { useWorkerFactory } from 'monaco-editor-wrapper/workerFactory';

export const configureMonacoWorkers = () => {
    useWorkerFactory({
        ignoreMapping: true,
        workerLoaders: {
            editorWorkerService: () => new Worker(new URL('monaco-editor/esm/vs/editor/editor.worker.js', import.meta.url), { type: 'module' }),
        }
    });
};

export const runClient = async () => {
    await initServices({
        serviceConfig: {
            userServices: {
                ...getThemeServiceOverride(),
                ...getTextmateServiceOverride(),
            },
            debugLogging: true,
        }
    });

    const parameters = window.parent.intinoDslEditorParameters();
    window.parent.intinoDslEditorInit(monaco, document.getElementById('monaco-editor-root'));
    initWebSocketAndStartClient(parameters.webSocketUrl);
    handleEvents();
};

let activeWebSocket = null;
let reconnectAttempts = 0;
let reconnectTimeout = null;

const MAX_RECONNECT_ATTEMPTS = 5;

export const initWebSocketAndStartClient = (url) => {
  const parameters = window.parent.intinoDslEditorParameters();
  if (parameters.language !== "tara") return;

  if (activeWebSocket && activeWebSocket.readyState < WebSocket.CLOSING) {
    console.warn("LSP already active. Skipping initialization.");
    return activeWebSocket;
  }

  console.log("[LSP] Connecting to " + url + "...");

  const webSocket = new WebSocket(url);
  activeWebSocket = webSocket;

  webSocket.onopen = () => {
    console.log("[LSP] Connected.");
    reconnectAttempts = 0;
    clearTimeout(reconnectTimeout);

    const socket = toSocket(webSocket);
    const reader = new WebSocketMessageReader(socket);
    const writer = new WebSocketMessageWriter(socket);
    const languageClient = createLanguageClient({ reader, writer });

    languageClient.start();

    reader.onClose(() => {
      console.warn("[LSP] Reader closed. Stopping language client.");
      languageClient.stop();
      activeWebSocket = null;
      scheduleReconnect(url);
    });
  };

  webSocket.onerror = (err) => {
    console.error("[LSP] Error:", err);
  };

  webSocket.onclose = (event) => {
    console.warn("[LSP] Connection closed (code: " + event.code + ".");
    activeWebSocket = null;
    scheduleReconnect(url);
  };

  return webSocket;
};

function scheduleReconnect(url) {
  if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
    console.error("[LSP] Max reconnection attempts reached. Giving up.");
    return;
  }

  const delay = Math.pow(2, reconnectAttempts) * 1000;
  reconnectAttempts++;
  console.log(`[WebSocket] Reconnecting in ${delay / 1000}s... (attempt ${reconnectAttempts})`);

  reconnectTimeout = setTimeout(() => {
    initWebSocketAndStartClient(url);
  }, delay);
}
export const createLanguageClient = (transports) => {
    return new MonacoLanguageClient({
        name: window.parent.intinoDslEditorParameters().language + ' client',
        clientOptions: {
            // use a language id as a document selector
            documentSelector: [window.parent.intinoDslEditorParameters().language],
            // disable the default error handler
            errorHandler: {
                error: () => ({ action: ErrorAction.Continue }),
                closed: () => ({ action: CloseAction.DoNotRestart })
            }
        },
        // create a language client connection from the JSON RPC connection on demand
        connectionProvider: {
            get: () => {
                return Promise.resolve(transports);
            }
        }
    });
};
const handleEvents = () => {
    const pressed = [];
    document.addEventListener("keydown", (event) => {
        pressed[event.key.toLowerCase()] = true;
        if ((pressed["control"] || pressed["meta"]) && event.key.toLowerCase() === "f9") {
            window.parent.intinoExecuteCommand("build");
            return false;
        }
    });
    document.addEventListener("keyup", (event) => {
        delete pressed[event.key.toLowerCase()];
    });
};
//# sourceMappingURL=wrapper.js.map