import { WebSocketServer, ServerOptions } from 'ws';
import { Server } from 'node:http';
import { URL } from 'node:url';
import { IWebSocket } from 'vscode-ws-jsonrpc';
import * as cp from 'child_process';
export declare enum LanguageName {
    /** https://nodejs.org/api/cli.html  */
    node = "node",
    /** https://docs.oracle.com/en/java/javase/21/docs/specs/man/java.html */
    java = "java"
}
export interface LanguageServerRunConfig {
    serverName: string;
    pathName: string;
    serverPort: number;
    runCommand: LanguageName | string;
    runCommandArgs: string[];
    wsServerOptions: ServerOptions;
    spawnOptions?: cp.SpawnOptions;
}
/**
 * start the language server inside the current process
 */
export declare const launchLanguageServer: (runconfig: LanguageServerRunConfig, socket: IWebSocket) => void;
export declare const upgradeWsServer: (runconfig: LanguageServerRunConfig, config: {
    server: Server;
    wss: WebSocketServer;
}) => void;
/**
 * Solves: __dirname is not defined in ES module scope
 */
export declare const getLocalDirectory: (referenceUrl: string | URL) => string;
//# sourceMappingURL=server-commons.d.ts.map