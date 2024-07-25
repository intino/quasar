import React from "react";
import { withStyles } from '@material-ui/core/styles';
import AbstractIntinoDslEditor from "../../gen/displays/AbstractIntinoDslEditor";
import IntinoDslEditorNotifier from "../../gen/displays/notifiers/IntinoDslEditorNotifier";
import IntinoDslEditorRequester from "../../gen/displays/requesters/IntinoDslEditorRequester";
import DisplayFactory from 'alexandria-ui-elements/src/displays/DisplayFactory';
import { withSnackbar } from 'notistack';
import * as monaco from 'monaco-editor';
import * as vscode from 'vscode';
import MonacoEditor from "react-monaco-editor";
import { toSocket, WebSocketMessageReader, WebSocketMessageWriter } from "vscode-ws-jsonrpc";
import { CloseAction, ErrorAction, MessageTransports, MonacoLanguageClient } from "monaco-languageclient";

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
	    return (
            <MonacoEditor
                width="100%"
                height="100%"
                language={'python'}
                theme={'custom-theme'}
                value={this.state.file.content}
                editorWillMount={this.handleBeforeMount.bind(this)}
                editorDidMount={this.handleMount.bind(this)}
            />
        );
    }

/*
	render() {
	    const id = this.props.id + "-editor";
	    window.setTimeout(() => {
            monaco.editor.create(document.getElementById(id), {
                value: this.state.file.content,
                language: this.state.file.language
            });
	    }, 100);
	    return (<div id={id} style={{height:"calc(100% - 5px)",width:"100%"}}></div>);
	}
	*/
/*
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
*/
    handleBeforeMount = (monaco) => {
        monaco.languages.register({
            id: 'python',
            extensions: ['.py'],
            aliases: ['PYTHON', 'python', 'py'],
        });
    };

    handleMount = (editor, monaco) => {
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
        webSocket.onopen = () => {
            const socket = toSocket(webSocket);
            const reader = new WebSocketMessageReader(socket);
            const writer = new WebSocketMessageWriter(socket);
            const languageClient = this.createLanguageClient({reader,writer});
            languageClient.start();
            reader.onClose(() => languageClient.stop());
        };
    }

    createLanguageClient = (transports) => {
        return new MonacoLanguageClient({
            name: "Sample Language Client",
            clientOptions: {
                // use a language id as a document selector
                documentSelector: ['python'],
                // disable the default error handler
                errorHandler: {
                    error: () => ({ action: ErrorAction.Continue }),
                    closed: () => ({ action: CloseAction.DoNotRestart }),
                }
            },
            connectionProvider: {
                get: () => {
                    return Promise.resolve(transports);
                }
            }
        });
    }
}

export default withStyles(styles, { withTheme: true })(withSnackbar(IntinoDslEditor));
DisplayFactory.register("IntinoDslEditor", withStyles(styles, { withTheme: true })(withSnackbar(IntinoDslEditor)));