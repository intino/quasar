import React from "react";
import { withStyles } from '@material-ui/core/styles';
import AbstractIntinoDslEditor from "../../gen/displays/AbstractIntinoDslEditor";
import IntinoDslEditorNotifier from "../../gen/displays/notifiers/IntinoDslEditorNotifier";
import IntinoDslEditorRequester from "../../gen/displays/requesters/IntinoDslEditorRequester";
import DisplayFactory from 'alexandria-ui-elements/src/displays/DisplayFactory';
import { withSnackbar } from 'notistack';
import AceEditor from 'react-ace';
import { AceLanguageClient } from "ace-linters/build/ace-language-client";
import "ace-builds/src-noconflict/theme-xcode";

ace.config.set("modePath", ace.config.get("modePath") + "/res/intino-dsl-editor")
ace.config.set("themePath", ace.config.get("modePath") + "/res/intino-dsl-editor")

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
             <AceEditor
                width="100%"
                height="100%"
                placeholder={this.translate("Empty file")}
                mode={this.state.file.language.toLowerCase()}
                name={this.props.id + "-editor"}
                fontSize={12}
                lineHeight={17}
                showPrintMargin={true}
                showGutter={true}
                highlightActiveLine={true}
                value={this.state.file.content}
                onLoad={this.handleMount.bind(this)}
                onChange={this.handleChange.bind(this)}
                enableSnippets={true}
                enableBasicAutocompletion={true}
                enableLiveAutoComplete={true}
                setOptions={{showLineNumbers: true, tabSize: 4 }}
             />
        );
    };

    handleMount = (editor) => {
        this.registerCommands(editor);
        this.registerLspServer(editor);
    };

    registerCommands = (editor) => {
        const save = this.handleSave.bind(this);
        editor.commands.addCommand({
            name: 'save',
            bindKey: {win: "Ctrl-S", "mac": "Cmd-S"},
            exec: function(editor) { save(editor.session.getValue()); }
        });
    };

    registerLspServer = (editor) => {
        const baseUrl = Application.configuration.baseUrl;
        const taraServerConfig = {
            module: () => import("ace-linters/build/language-client"),
            modes: "tara",
            type: "socket",
            socket: new WebSocket(baseUrl + "/dsl/tara")
        };
        AceLanguageClient.for(taraServerConfig).registerEditor(editor);
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

}

export default withStyles(styles, { withTheme: true })(withSnackbar(IntinoDslEditor));
DisplayFactory.register("IntinoDslEditor", withStyles(styles, { withTheme: true })(withSnackbar(IntinoDslEditor)));