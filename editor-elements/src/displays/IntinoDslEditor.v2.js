import React from "react";
import { withStyles } from '@material-ui/core/styles';
import AbstractIntinoDslEditor from "../../gen/displays/AbstractIntinoDslEditor";
import IntinoDslEditorNotifier from "../../gen/displays/notifiers/IntinoDslEditorNotifier";
import IntinoDslEditorRequester from "../../gen/displays/requesters/IntinoDslEditorRequester";
import DisplayFactory from 'alexandria-ui-elements/src/displays/DisplayFactory';
import { withSnackbar } from 'notistack';
import Editor from '@monaco-editor/react';
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
import { MonacoLanguageClient, CloseAction, ErrorAction, MonacoServices } from 'monaco-languageclient';
import { listen } from 'vscode-ws-jsonrpc';

const styles = theme => ({});

class IntinoDslEditor extends AbstractIntinoDslEditor {

	constructor(props) {
		super(props);
		this.notifier = new IntinoDslEditorNotifier(this);
		this.requester = new IntinoDslEditorRequester(this);
		this.state = {
		    file: { name: "default.tara", content: "", language: "tara" }
		};
	};

    render() {
        const options = { selectOnLineNumbers: true, automaticLayout: true, };
        return (
            <Editor
                width="100%"
                height="100%"
                theme="vs-light"
                language={this.state.file.language.toLowerCase()}
                value={this.state.file.content}
                options={options}
                onChange={this.handleChange.bind(this)}
                onMount={this.handleMount.bind(this)}
              />
        );
    };

    handleMount = (editor, monaco) => {
        MonacoServices.install(editor);
        this.registerCommands(editor);
        this.createWebSocket();
    };

    registerCommands = (editor) => {
        editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, this.handleSave.bind(this));
    };

    handleSave = (value) => {
        this.requester.fileContent(this.state.file.content);
    };

    handleChange = (content) => {
        this.state.file.content = content;
        this.setState({file: this.state.file})
        this.requester.fileModified();
    };

    receiveContent = () => {
        this.requester.fileContent(this.state.file.content);
    };

    refresh = (file) => {
        this.setState({file});
    };

    createWebSocket = () => {
        const baseUrl = Application.configuration.baseUrl;
        const webSocket = new WebSocket(baseUrl + "/dsl/tara");
        listen({
            webSocket,
            onConnection: (connection) => {
                const languageClient = this.createLanguageClient(connection);
                languageClient.start();
                connection.onClose(() => languageClient.stop());
            }
        });
        return webSocket;
    }

    createLanguageClient = (connection) => {
        return new MonacoLanguageClient({
            name: "Sample Language Client",
            clientOptions: {
                // use a language id as a document selector
                documentSelector: ['javascript', 'typescript'],
                // disable the default error handler
                errorHandler: {
                    error: () => ErrorAction.Continue,
                    closed: () => CloseAction.DoNotRestart
                }
            },
            connectionProvider: {
                get: (errorHandler, closeHandler) => {
                    return Promise.resolve({
                        listen: (callback) => {
                            connection.onClose(closeHandler);
                            connection.onError(errorHandler);
                            connection.onNotification(callback);
                            connection.onRequest(callback);
                            connection.onProgress(callback);
                        },
                        send: connection.sendRequest.bind(connection),
                        onClose: connection.onClose.bind(connection),
                        onError: connection.onError.bind(connection),
                    });
                }
            }
        });
    }
}

export default withStyles(styles, { withTheme: true })(withSnackbar(IntinoDslEditor));
DisplayFactory.register("IntinoDslEditor", withStyles(styles, { withTheme: true })(withSnackbar(IntinoDslEditor)));