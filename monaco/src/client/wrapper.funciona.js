/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2024 TypeFox and others.
 * Licensed under the MIT License. See LICENSE in the package root for license information.
 * ------------------------------------------------------------------------------------------ */
import getKeybindingsServiceOverride from '@codingame/monaco-vscode-keybindings-service-override';
// this is required syntax highlighting
import '@codingame/monaco-vscode-json-default-extension';
import { MonacoEditorLanguageClientWrapper } from 'monaco-editor-wrapper';
import { useWorkerFactory } from 'monaco-editor-wrapper/workerFactory';
export const configureMonacoWorkers = () => {
    // override the worker factory with your own direct definition
    useWorkerFactory({
        ignoreMapping: true,
        workerLoaders: {
            editorWorkerService: () => new Worker(new URL('monaco-editor/esm/vs/editor/editor.worker.js', import.meta.url), { type: 'module' })
        }
    });
};
const text = `{
    "$schema": "http://json.schemastore.org/coffeelint",
    "line_endings": {"value": "unix"}
}`;
export const jsonClientUserConfig = {
    wrapperConfig: {
        serviceConfig: {
            userServices: {
                ...getKeybindingsServiceOverride(),
            },
            debugLogging: true
        },
        editorAppConfig: {
            $type: 'extended',
            codeResources: {
                main: {
                    text,
                    fileExt: 'json'
                }
            },
            useDiffEditor: false,
            userConfiguration: {
                json: JSON.stringify({
                    'workbench.colorTheme': 'Default Dark Modern',
                    'editor.guides.bracketPairsHorizontal': 'active',
                    'editor.lightbulb.enabled': 'On',
                    'editor.wordBasedSuggestions': 'off'
                })
            }
        }
    },
    languageClientConfig: {
        languageId: 'json',
        options: {
            $type: 'WebSocketUrl',
            url: 'ws://localhost:30000/sampleServer',
            startOptions: {
                onCall: () => {
                    console.log('Connected to socket.');
                },
                reportStatus: true
            },
            stopOptions: {
                onCall: () => {
                    console.log('Disconnected from socket.');
                },
                reportStatus: true
            }
        }
    }
};
export const runJsonWrapper = () => {
    const wrapper = new MonacoEditorLanguageClientWrapper();
    const htmlElement = document.getElementById('monaco-editor-root');
    const webSocketUrl = getParamValue("web-socket-url");
    jsonClientUserConfig.languageClientConfig.options.url = webSocketUrl;
    try {
        wrapper.dispose();
        wrapper.initAndStart(jsonClientUserConfig, htmlElement);
    }
    catch (e) {
        console.error(e);
    }
};
function getParamValue(paramName) {
    var url = window.location.search.substring(1);
    var qArray = url.split('&');
    for (var i = 0; i < qArray.length; i++) {
        var pArr = qArray[i].split('='); //split key and value
        if (pArr[0] == paramName)
            return pArr[1]; //return value
    }
}
//# sourceMappingURL=wrapper.js.map