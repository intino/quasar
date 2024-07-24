/* --------------------------------------------------------------------------------------------
 * Copyright (c) 2024 TypeFox and others.
 * Licensed under the MIT License. See LICENSE in the package root for license information.
 * ------------------------------------------------------------------------------------------ */
import { WebSocketServer } from 'ws';
import express from 'express';
import { getLocalDirectory, upgradeWsServer } from './server-commons.js';
/** LSP server runner */
export const runLanguageServer = (languageServerRunConfig) => {
    process.on('uncaughtException', err => {
        console.error('Uncaught Exception: ', err.toString());
        if (err.stack !== undefined) {
            console.error(err.stack);
        }
    });
    // create the express application
    const app = express();
    // server the static content, i.e. index.html
    const dir = getLocalDirectory(import.meta.url);
    app.use(express.static(dir));
    // start the http server
    const httpServer = app.listen(languageServerRunConfig.serverPort);
    const wss = new WebSocketServer(languageServerRunConfig.wsServerOptions);
    // create the web socket
    upgradeWsServer(languageServerRunConfig, {
        server: httpServer,
        wss
    });
};
//# sourceMappingURL=language-server-runner.js.map