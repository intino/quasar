/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2024 TypeFox and others.
 * Licensed under the MIT License. See LICENSE in the package root for license information.
 * ------------------------------------------------------------------------------------------ */
import * as vscode from 'vscode';
import getEditorServiceOverride from '@codingame/monaco-vscode-editor-service-override';
import getKeybindingsServiceOverride from '@codingame/monaco-vscode-keybindings-service-override';
import '@codingame/monaco-vscode-python-default-extension';
import { useOpenEditorStub } from 'monaco-editor-wrapper/vscode/services';
export const createUserConfig = (workspaceRoot, code, codeUri) => {
    return {
        languageClientConfig: {
            languageId: 'python',
            name: 'Python Language Server Example',
            options: {
                $type: 'WebSocket',
                host: 'localhost',
                port: 30001,
                path: 'pyright',
                extraParams: {
                    authorization: 'UserAuth'
                },
                secured: false,
                startOptions: {
                    onCall: (languageClient) => {
                        setTimeout(() => {
                            ['pyright.restartserver', 'pyright.organizeimports'].forEach((cmdName) => {
                                vscode.commands.registerCommand(cmdName, (...args) => {
                                    languageClient?.sendRequest('workspace/executeCommand', { command: cmdName, arguments: args });
                                });
                            });
                        }, 250);
                    },
                    reportStatus: true,
                }
            },
            clientOptions: {
                documentSelector: ['python'],
                workspaceFolder: {
                    index: 0,
                    name: 'workspace',
                    uri: vscode.Uri.parse(workspaceRoot)
                },
            },
        },
        wrapperConfig: {
            serviceConfig: {
                userServices: {
                    ...getEditorServiceOverride(useOpenEditorStub),
                    ...getKeybindingsServiceOverride()
                },
                debugLogging: true
            },
            editorAppConfig: {
                $type: 'extended',
                codeResources: {
                    main: {
                        text: code,
                        uri: codeUri
                    }
                },
                userConfiguration: {
                    json: JSON.stringify({
                        'workbench.colorTheme': 'Default Dark Modern',
                        'editor.guides.bracketPairsHorizontal': 'active',
                        'editor.wordBasedSuggestions': 'off'
                    })
                },
                useDiffEditor: false
            }
        },
        loggerConfig: {
            enabled: true,
            debugEnabled: true
        }
    };
};
//# sourceMappingURL=config.js.map