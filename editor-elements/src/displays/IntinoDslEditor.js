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
import history from "alexandria-ui-elements/src/util/History";

const styles = theme => ({});

class IntinoDslEditor extends AbstractIntinoDslEditor {

	constructor(props) {
		super(props);
		this.notifier = new IntinoDslEditorNotifier(this);
		this.requester = new IntinoDslEditorRequester(this);
		this.loading = React.createRef();
		this.state = {
		    info: { dslName: "meta", modelName: "meta", modelRelease: "Draft", fileAddress: "" },
		    countFiles: 0,
		    file: { name: "default.tara", uri: "default.tara", content: "", language: "tara" }
		};
	};

	render() {
	    if (this.state.countFiles == 0) return (<React.Fragment/>);
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
	    window.intinoDslEditorInit = this.handleInit.bind(this);
	    window.intinoExecuteCommand = this.handleExecuteCommand.bind(this);
        return (
            <div style={{position:'relative',height:"calc(100% - 20px)",width:"100%"}}>
                <div className="layout vertical flex center-center" ref={this.loading} style={{height:"100%",width:"100%",position:"absolute",background:backgroundColor,top:'0',display:'block'}}>
                    <BarLoader color={theme.palette.secondary.main} width={400} loading={true}/>
                </div>
                <iframe src={Application.configuration.url + "/res/monaco.html?mode=" + (theme.isDark() ? "dark" : "light")/*?id=" + id + "&m=" + Math.random()*/}
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
            webSocketUrl: Application.configuration.baseUrl.replace("http", "ws") + "/dsl/tara?dsl=" + this.state.info.dslName + "&model=" + this.state.info.modelName + "&model-release=" + this.state.info.modelRelease,
            language: this.selectedFile.language,
            readonly: this.state.info.readonly,
        }
    };

    handleInit = (monaco, container) => {
        this.createEditor(monaco, container);
        this.registerModel(this.selectedFile, monaco);
        if (this.selectedFile != null) this.updatePosition(this.selectedFile.position);
    };

    createEditor = (monaco, container) => {
        const CtrlCmd = 2048; const KeyS = 49;
        const refreshFooter = this.refreshFooter.bind(this);
        const theme = Theme.get();
        monaco.editor.registerEditorOpener(this.editorOpenerListener(monaco));
        monaco.languages.register({id:"tara", extensions: ['.tara'], aliases: ["TARA", "tara"]});
        monaco.languages.setLanguageConfiguration('tara', {
            autoClosingPairs: [
                { open: '{', close: '}' },
                { open: '[', close: ']' },
                { open: '(', close: ')' },
                { open: '"', close: '"', notIn: ['string', 'comment'] }
            ],
            surroundingPairs: [
                { open: '{', close: '}' },
                { open: '[', close: ']' },
                { open: '(', close: ')' },
                { open: '"', close: '"' }
            ],
        });
        this.monaco = monaco;
        this.editor = monaco.editor.create(container, {
            automaticLayout: true,
            theme: "vs-dark",
            readOnly: this.state.info.readonly,
            domReadOnly: this.state.info.readonly,
            insertSpaces: false, tabSize: 4,
            scrollBeyondLastLine: false,
            detectIndentation: false,
            suggestOnTriggerCharacters: true,
            quickSuggestions: true,
            wordBasedSuggestions: 'off',
            autoClosingBrackets: 'always',
            autoClosingQuotes: true,
            autoSurround: "languageDefined",
            parameterHints: { enabled: true }
        });
        this.editor.addCommand(CtrlCmd | KeyS, this.handleSave.bind(this));
        this.editor.onDidChangeCursorPosition(e => refreshFooter(e));
        this.setupTheme(monaco);
    };

    editorOpenerListener = (monaco) => {
        const self = this;
        return {
            openCodeEditor: (editor, file) => {
                monaco.editor.getModels().forEach(m => m.dispose());
                this.registerModel(self.state.files[file.path.substring(1)], monaco);
                self.notifyFileChange(file.path.substring(1));
                self.state.selectedFile = self.state.files[file.path.substring(1)];
            }
        };
    }

    notifyFileChange = (uri) => {
        history.stopListening();
        history.push(this.state.info.fileAddress.replace(/:file/, uri), {});
        history.continueListening();
        this.requester.fileSelected(uri);
    };

    setupTheme = (monaco) => {
        const theme = Theme.get();
        const loading = this.loading;
        loading.current.style.display = "block";
        if (!theme.isDark()) {
            loading.current.style.display = "none";
            return;
        }
        window.setTimeout(() => {
            monaco.editor.setTheme("Default Dark Modern");
            window.setTimeout(() => loading.current.style.display = "none", 100);
        }, 250);
    };

    registerModel = (file, monaco) => {
        const handleChange = this.handleChange.bind(this);
        const model = monaco.editor.createModel(file.content, file.language.toLowerCase(), monaco.Uri.parse(file.uri));
        model.onDidChangeContent(event => handleChange(file, this.editor.getValue()));
        //model.updateOptions({ insertSpaces: false, tabSize: 4 });
        this.editor.setModel(model);
        window.setTimeout(e => this.editor.focus(), 10);
    };

    refreshFooter = (event) => {
        const lineNumberElement = document.getElementById(this.props.id + "_line_number");
        const columnElement = document.getElementById(this.props.id + "_column");
        lineNumberElement.innerHTML = event.position.lineNumber;
        columnElement.innerHTML = event.position.column;
    };

    handleSave = (value) => {
        this.requester.fileContent({file: this.selectedFile.uri, content: this.selectedFile.content});
    };

    handleExecuteCommand = (command) => {
        this.requester.executeCommand(command);
    };

    handleChange = (file, content) => {
        this.selectedFile.content = content;
        this.requester.fileModified();
    };

    handleGotoToken = (uri, position, token) => {
        this.requester.gotoToken({ fromFile: uri, fromPosition: position, token: token });
    };

    receiveContent = () => {
        this.requester.fileContent({file: this.selectedFile.uri, content: this.selectedFile.content});
    };

    setup = (info) => {
        this.selectedFile = null;
        let fileMap = {};
        for (var i=0; i<info.files.length; i++) {
            fileMap[info.files[i].uri] = info.files[i];
            if (info.files[i].active) this.selectedFile = info.files[i];
        }
        this.setState({info, files: fileMap, countFiles: info.files.length });
    };

    refreshFile = (file) => {
        this.updateFile(file);
    };

    refreshReadonly = (readonly) => {
        this.editor.updateOptions({ readOnly: readonly, domReadOnly: readonly });
    };

    updateFile = (file) => {
        this.selectedFile = file;
        if (file == null) return;
        this.monaco.editor.getModels().forEach(m => m.dispose());
        this.editor.setModel(null);
        this.registerModel(file, this.monaco);
        this.updatePosition(file.position);
    };

    updatePosition = (position) => {
        if (position == null) return;
        this.editor.setPosition({lineNumber: position.line, column: position.column != -1 ? position.column : 1});
        this.editor.revealLine(position.line);
    };

}

export default withStyles(styles, { withTheme: true })(withSnackbar(IntinoDslEditor));
DisplayFactory.register("IntinoDslEditor", withStyles(styles, { withTheme: true })(withSnackbar(IntinoDslEditor)));