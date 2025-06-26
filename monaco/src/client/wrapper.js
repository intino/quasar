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
/** parameterized version , support all languageId */
export const initWebSocketAndStartClient = (url) => {
    const parameters = window.parent.intinoDslEditorParameters();
    if (parameters.language != "tara") return;
    const webSocket = new WebSocket(url);
    webSocket.onopen = () => {
        const socket = toSocket(webSocket);
        const reader = new WebSocketMessageReader(socket);
        const writer = new WebSocketMessageWriter(socket);
        const languageClient = createLanguageClient({reader,writer});
        languageClient.start();
        reader.onClose(() => languageClient.stop());
    };
    return webSocket;
};
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