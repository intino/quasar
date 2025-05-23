import React from "react";
import { withStyles } from '@material-ui/core/styles';
import AbstractGoogleLoginDisplay from "../../gen/displays/AbstractGoogleLoginDisplay";
import GoogleLoginDisplayNotifier from "../../gen/displays/notifiers/GoogleLoginDisplayNotifier";
import GoogleLoginDisplayRequester from "../../gen/displays/requesters/GoogleLoginDisplayRequester";
import DisplayFactory from 'alexandria-ui-elements/src/displays/DisplayFactory';
import { withSnackbar } from 'notistack';
import { GoogleOAuthProvider } from '@react-oauth/google';
import { GoogleLogin } from '@react-oauth/google';

const styles = theme => ({});

class GoogleLoginDisplay extends AbstractGoogleLoginDisplay {

	constructor(props) {
		super(props);
		this.notifier = new GoogleLoginDisplayNotifier(this);
		this.requester = new GoogleLoginDisplayRequester(this);
		this.state = {
		    ...this.state,
		    clientId: null
		}
	};

	render() {
	    if (this.state.clientId == null) return (<div>this.translate("You must define google client id in application run arguments")</div>);
	    return (
	        <GoogleOAuthProvider clientId={this.state.clientId}>
	            <GoogleLogin onSuccess={this.handleLoginSuccess.bind(this)} onError={this.handleLoginError.bind(this)} auto_select/>
            </GoogleOAuthProvider>
        );
	};

	refresh = (clientId) => {
    	this.setState({clientId});
	};

	handleLoginSuccess = (response) => {
	    this.requester.success(response.credential);
	};

	handleLoginError = () => {
        this.requester.error();
	};

}

export default withStyles(styles, { withTheme: true })(withSnackbar(GoogleLoginDisplay));
DisplayFactory.register("GoogleLoginDisplay", withStyles(styles, { withTheme: true })(withSnackbar(GoogleLoginDisplay)));