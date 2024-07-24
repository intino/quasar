import { MonacoLanguageClient, IConnectionProvider } from 'monaco-languageclient';
import { LanguageClientOptions, MessageTransports } from 'vscode-languageclient/lib/common/client.js';
import { Logger } from 'monaco-languageclient/tools';
import { WebSocketConfigOptions, WebSocketConfigOptionsUrl, WorkerConfigDirect, WorkerConfigOptions } from './commonTypes.js';
export type LanguageClientConfig = {
    languageId: string;
    options: WebSocketConfigOptions | WebSocketConfigOptionsUrl | WorkerConfigOptions | WorkerConfigDirect;
    name?: string;
    clientOptions?: LanguageClientOptions;
    connectionProvider?: IConnectionProvider;
};
export type LanguageClientError = {
    message: string;
    error: Error | string;
};
export declare class LanguageClientWrapper {
    private languageClient?;
    private languageClientConfig?;
    private messageTransports?;
    private connectionProvider?;
    private languageId;
    private worker?;
    private port?;
    private name?;
    private logger;
    init(config: {
        languageClientConfig: LanguageClientConfig;
        logger?: Logger;
    }): Promise<void>;
    haveLanguageClient(): boolean;
    haveLanguageClientConfig(): boolean;
    getLanguageClient(): MonacoLanguageClient | undefined;
    getWorker(): Worker | undefined;
    isStarted(): boolean;
    getMessageTransports(): MessageTransports | undefined;
    getConnectionProvider(): IConnectionProvider | undefined;
    start(): Promise<void>;
    /**
     * Restart the languageclient with options to control worker handling
     *
     * @param updatedWorker Set a new worker here that should be used. keepWorker has no effect then, as we want to dispose of the prior workers
     * @param keepWorker Set to true if worker should not be disposed
     */
    restartLanguageClient(updatedWorker?: Worker, keepWorker?: boolean): Promise<void>;
    private startLanguageClientConnection;
    private handleLanguageClientStart;
    private disposeWorker;
    disposeLanguageClient(keepWorker?: boolean): Promise<void>;
    reportStatus(): string[];
}
//# sourceMappingURL=languageClientWrapper.d.ts.map