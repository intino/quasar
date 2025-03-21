import React from "react";
import { withStyles } from '@material-ui/core/styles';
import AbstractIntinoDslEditor from "../../gen/displays/AbstractIntinoDslEditor";
import IntinoDslEditorNotifier from "../../gen/displays/notifiers/IntinoDslEditorNotifier";
import IntinoDslEditorRequester from "../../gen/displays/requesters/IntinoDslEditorRequester";
import DisplayFactory from 'alexandria-ui-elements/src/displays/DisplayFactory';
import { withSnackbar } from 'notistack';
import Theme from "app-elements/gen/Theme";
import { BarLoader } from "react-spinners";
import 'alexandria-ui-elements/res/styles/layout.css';

const styles = theme => ({});

class IntinoDslEditor extends AbstractIntinoDslEditor {

	constructor(props) {
		super(props);
		this.notifier = new IntinoDslEditorNotifier(this);
		this.requester = new IntinoDslEditorRequester(this);
		this.loading = React.createRef();
		this.state = {
		    file: { name: "default.tara", uri: "default.tara", content: "", language: "tara" }
		};
	};

	render() {
        return (
            <div style={{position:'relative',height:"100%",width:"100%"}}>
                {this.renderEditor()}
                {this.renderFooter()}
            </div>
        );
    };

	renderEditor = () => {
	    const id = this.props.id;
	    const theme = Theme.get();
	    const backgroundColor = theme.isDark() ? "#1F1F1F" : "white";
	    window.intinoDslEditorParameters = this.getParameters.bind(this);
	    window.intinoDslEditorSetup = this.handleSetup.bind(this);
        return (
            <div style={{position:'relative',height:"calc(100% - 20px)",width:"100%"}}>
                <div className="layout vertical flex center-center" ref={this.loading} style={{height:"100%",width:"100%",position:"absolute",background:backgroundColor,top:'0',display:'block'}}>
                    <BarLoader color={theme.palette.secondary.main} width={400} loading={true}/>
                </div>
                <iframe src={Application.configuration.url + "/res/monaco.html?id=" + id + "&m=" + Math.random()}
                    style={{height:"calc(100% - 4px)",width:"100%",border:0}}/>
            </div>
        );
    };

    renderFooter = () => {
        return (
            <div className="layout horizontal end-justified" style={{height:"20px",width:"100%",padding:'0 15px'}}>
                <div id={this.props.id + "_line_number"}></div>
                <div>:</div>
                <div id={this.props.id + "_column"}></div>
            </div>
        );
    };

    getParameters = () => {
        const theme = Theme.get();
	    return {
            darkMode: theme.isDark(),
            webSocketUrl: Application.configuration.baseUrl.replace("http", "ws") + "/dsl/tara?dsl=" + this.state.file.dslName + "&model=" + this.state.file.model,
            file: this.state.file,
        }
    };

    handleSetup = (editor, monaco) => {
        const theme = Theme.get();
        const handleChange = this.handleChange.bind(this);
        const CtrlCmd = 2048;
        const KeyS = 49;
        const KeyF = 70;
        const self = this;
        editor.getModel().onDidChangeContent(event => handleChange(editor.getValue()));
        editor.getModel().updateOptions({ insertSpaces: false, tabSize: 4 });
        editor.addCommand(CtrlCmd | KeyS, this.handleSave.bind(this));
        editor.onDidChangeCursorPosition(e => self.refreshFooter(e));
        const loading = this.loading;
        loading.current.style.display = "block";
        window.setTimeout(() => {
            monaco.editor.setTheme(theme.isDark() ? "Default Dark Modern" : "Default Light Modern");
            window.setTimeout(() => loading.current.style.display = "none", 80);
        }, 80);
    };

    refreshFooter = (event) => {
        const lineNumberElement = document.getElementById(this.props.id + "_line_number");
        const columnElement = document.getElementById(this.props.id + "_column");
        lineNumberElement.innerHTML = event.position.lineNumber;
        columnElement.innerHTML = event.position.column;
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