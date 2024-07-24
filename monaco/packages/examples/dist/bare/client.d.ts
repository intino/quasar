import '@codingame/monaco-vscode-theme-defaults-default-extension';
import '@codingame/monaco-vscode-json-default-extension';
import { MonacoLanguageClient } from 'monaco-languageclient';
import { MessageTransports } from 'vscode-languageclient';
export declare const configureMonacoWorkers: () => void;
export declare const runClient: () => Promise<void>;
/** parameterized version , support all languageId */
export declare const initWebSocketAndStartClient: (url: string) => WebSocket;
export declare const createLanguageClient: (transports: MessageTransports) => MonacoLanguageClient;
//# sourceMappingURL=client.d.ts.map