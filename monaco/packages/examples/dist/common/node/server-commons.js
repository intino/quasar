import { URL } from 'node:url';
import { dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { WebSocketMessageReader, WebSocketMessageWriter } from 'vscode-ws-jsonrpc';
import { createConnection, createServerProcess, forward } from 'vscode-ws-jsonrpc/server';
import { Message, InitializeRequest } from 'vscode-languageserver';
export var LanguageName;
(function (LanguageName) {
    /** https://nodejs.org/api/cli.html  */
    LanguageName["node"] = "node";
    /** https://docs.oracle.com/en/java/javase/21/docs/specs/man/java.html */
    LanguageName["java"] = "java";
})(LanguageName || (LanguageName = {}));
/**
 * start the language server inside the current process
 */
export const launchLanguageServer = (runconfig, socket) => {
    const { serverName, runCommand, runCommandArgs, spawnOptions } = runconfig;
    // start the language server as an external process
    const reader = new WebSocketMessageReader(socket);
    const writer = new WebSocketMessageWriter(socket);
    const socketConnection = createConnection(reader, writer, () => socket.dispose());
    const serverConnection = createServerProcess(serverName, runCommand, runCommandArgs, spawnOptions);
    if (serverConnection) {
        forward(socketConnection, serverConnection, message => {
            if (Message.isRequest(message)) {
                console.log(`${serverName} Server received:`);
                console.log(message);
                if (message.method === InitializeRequest.type.method) {
                    const initializeParams = message.params;
                    initializeParams.processId = process.pid;
                }
            }
            if (Message.isResponse(message)) {
                console.log(`${serverName} Server sent:`);
                console.log(message);
            }
            return message;
        });
    }
};
export const upgradeWsServer = (runconfig, config) => {
    config.server.on('upgrade', (request, socket, head) => {
        const baseURL = `http://${request.headers.host}/`;
        const pathName = request.url !== undefined ? new URL(request.url, baseURL).pathname : undefined;
        if (pathName === runconfig.pathName) {
            config.wss.handleUpgrade(request, socket, head, webSocket => {
                const socket = {
                    send: content => webSocket.send(content, error => {
                        if (error) {
                            throw error;
                        }
                    }),
                    onMessage: cb => webSocket.on('message', (data) => {
                        console.log(data.toString());
                        cb(data);
                    }),
                    onError: cb => webSocket.on('error', cb),
                    onClose: cb => webSocket.on('close', cb),
                    dispose: () => webSocket.close()
                };
                // launch the server when the web socket is opened
                if (webSocket.readyState === webSocket.OPEN) {
                    launchLanguageServer(runconfig, socket);
                }
                else {
                    webSocket.on('open', () => {
                        launchLanguageServer(runconfig, socket);
                    });
                }
            });
        }
    });
};
/**
 * Solves: __dirname is not defined in ES module scope
 */
export const getLocalDirectory = (referenceUrl) => {
    const __filename = fileURLToPath(referenceUrl);
    return dirname(__filename);
};
//# sourceMappingURL=server-commons.js.map