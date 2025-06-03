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
	     return (<Editor containerProps={{ style: { resize: 'vertical' } }} value={this.content()} onChange={this.handleChange.bind(this)} />);
	};

	refresh = (content) => {
	    this.setState({content});
	};

	handleChange = (e) => {
	    this.setState({content: e.target.value});
	    this.requester.update(e.target.value);
	};

    content = () => {
        const result = this.state.content;
        const isDark = Theme.get().isDark();
        const content = "<div class='help'>" + result + "</div>";
        return isDark ? this.addDark(content) : content;
    };

    addDark = (content) => {
        return "<div class='dark'>" + content + "</div>";
    };

}

export default withStyles(styles, { withTheme: true })(withSnackbar(HelpEditor));
DisplayFactory.register("HelpEditor", withStyles(styles, { withTheme: true })(withSnackbar(HelpEditor)));