import React from "react";
import { withStyles } from '@material-ui/core/styles';
import AbstractIntinoDslEditor from "../../gen/displays/AbstractIntinoDslEditor";
import IntinoDslEditorNotifier from "../../gen/displays/notifiers/IntinoDslEditorNotifier";
import IntinoDslEditorRequester from "../../gen/displays/requesters/IntinoDslEditorRequester";
import DisplayFactory from 'alexandria-ui-elements/src/displays/DisplayFactory';
import { withSnackbar } from 'notistack';

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
	    const id = this.props.id;
	    window.intinoDslEditorParameters = this.getParameters.bind(this);
	    window.intinoDslEditorSetup = this.handleSetup.bind(this);
        return (
            <iframe src={Application.configuration.url + "/res/monaco.html?id=" + id + "&m=" + Math.random()}
                    style={{height:"calc(100% - 4px)",width:"100%",border:0}}/>
        );
    };

    getParameters = () => {
	    const wsUrl = Application.configuration.baseUrl.replace("http", "ws") + "/dsl/tara";
	    //const wsUrl = "ws://localhost:30000/sampleServer";
        return {
            webSocketUrl: wsUrl,
            content: this.state.file.content,
            language: this.state.file.language,
        }
    };

    handleSetup = (editor) => {
        const handleChange = this.handleChange.bind(this);
        const CtrlCmd = 2048;
        const KeyS = 49;
        editor.getModel().onDidChangeContent(event => handleChange(editor.getValue()));
        editor.addCommand(CtrlCmd | KeyS, this.handleSave.bind(this));
    };

    handleSave = (value) => {
        this.requester.fileContent(this.state.file.content);
    };

    handleChange = (content) => {
        this.state.file.content = content;
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