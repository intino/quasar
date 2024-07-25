import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
import { MonacoLanguageClient, CloseAction, ErrorAction, MonacoServices } from 'monaco-languageclient';
import { listen } from 'vscode-ws-jsonrpc';

function createWebSocket(url) {
  const webSocket = new WebSocket(url);
  listen({
    webSocket,
    onConnection: (connection) => {
      const languageClient = createLanguageClient(connection);
      languageClient.start();
      connection.onClose(() => languageClient.stop());
    }
  });
  return webSocket;
}

function createLanguageClient(connection) {
  return new MonacoLanguageClient({
    name: "Monaco Language Client",
    clientOptions: {
      documentSelector: ['javascript', 'typescript'],
      errorHandler: {
        error: () => ErrorAction.Continue,
        closed: () => CloseAction.Restart
      }
    },
    connectionProvider: {
      get: (errorHandler, closeHandler) => {
        return Promise.resolve({
          listen: (callback) => {
            connection.listen(callback);
          },
          send: (msg) => connection.send(msg),
          onClose: connection.onClose.bind(connection),
          onDispose: connection.onDispose.bind(connection)
        });
      }
    }
  });
}

export function connectToLanguageServer(url) {
  createWebSocket(url);
}