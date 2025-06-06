import React from "react";
import { withStyles } from '@material-ui/core/styles';
import AbstractHelpEditor from "../../gen/displays/AbstractHelpEditor";
import HelpEditorNotifier from "../../gen/displays/notifiers/HelpEditorNotifier";
import HelpEditorRequester from "../../gen/displays/requesters/HelpEditorRequester";
import DisplayFactory from 'alexandria-ui-elements/src/displays/DisplayFactory';
import { withSnackbar } from 'notistack';
import Editor from 'react-simple-wysiwyg';
import Theme from 'app-elements/gen/Theme';

const styles = theme => ({});

class HelpEditor extends AbstractHelpEditor {

	constructor(props) {
		super(props);
		this.notifier = new HelpEditorNotifier(this);
		this.requester = new HelpEditorRequester(this);
		this.state = {
		    ...this.state,
		    content: ""
		}
	};

	render() {
	     window.setTimeout(() => this.addDarkMode(), 1);
	     return (<Editor containerProps={{ style: { resize: 'vertical' } }} value={this.state.content} onChange={this.handleChange.bind(this)} />);
	};

	refresh = (content) => {
	    this.setState({content});
	};

	handleChange = (e) => {
	    this.setState({content: e.target.value});
	    this.requester.update(e.target.value);
	};

    addDarkMode = () => {
        const isDark = Theme.get().isDark();
        if (!isDark) return;
        const elements = document.getElementsByClassName("rsw-editor");
        if (elements.length == 0) return;
        elements[0].className = "rsw-editor dark";
    };

}

export default withStyles(styles, { withTheme: true })(withSnackbar(HelpEditor));
DisplayFactory.register("HelpEditor", withStyles(styles, { withTheme: true })(withSnackbar(HelpEditor)));