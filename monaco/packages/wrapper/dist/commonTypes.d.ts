import { MonacoLanguageClient } from 'monaco-languageclient';
export type WebSocketCallOptions = {
    /** Adds handle on languageClient */
    onCall: (languageClient?: MonacoLanguageClient) => void;
    /** Reports Status Of Language Client */
    reportStatus?: boolean;
};
export type LanguageClientConfigType = 'WebSocket' | 'WebSocketUrl' | 'WorkerConfig' | 'Worker';
export type WebSocketUrl = {
    secured: boolean;
    host: string;
    port?: number;
    path?: string;
};
export type WebSocketConfigOptions = {
    $type: 'WebSocket';
    secured: boolean;
    host: string;
    port?: number;
    path?: string;
    extraParams?: Record<string, string | number | Array<string | number>>;
    startOptions?: WebSocketCallOptions;
    stopOptions?: WebSocketCallOptions;
};
export type WebSocketConfigOptionsUrl = {
    $type: 'WebSocketUrl';
    url: string;
    startOptions?: WebSocketCallOptions;
    stopOptions?: WebSocketCallOptions;
};
export type WorkerConfigOptions = {
    $type: 'WorkerConfig';
    url: URL;
    type: 'classic' | 'module';
    messagePort?: MessagePort;
    workerName?: string;
};
export type WorkerConfigDirect = {
    $type: 'WorkerDirect';
    worker: Worker;
    messagePort?: MessagePort;
};
//# sourceMappingURL=commonTypes.d.ts.map