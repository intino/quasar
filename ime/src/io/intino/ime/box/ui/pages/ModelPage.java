package io.intino.ime.box.ui.pages;

import io.intino.alexandria.ui.services.push.User;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.displays.templates.ModelTemplate;
import io.intino.ime.model.Model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ModelPage extends AbstractModelPage {
	public String user;
	public String name;
	public String version;
	public String file;
	public String accessToken;

	@Override
	public boolean hasPermissions() {
		if (PathHelper.PublicUser.equals(user)) return true;
		Model model = box.modelManager().model(Model.id(name, version));
		if (model == null) return false;
		if (model.isPublic()) return true;
		String token = model.token();
		if (accessToken != null && accessToken.equals(token)) return true;
		User loggedUser = session.user();
		return loggedUser != null && loggedUser.username().equals(user);
	}

	@Override
	public String redirectUrl() {
		String callbackUrl = URLEncoder.encode(session.browser().requestUrl(), StandardCharsets.UTF_8);
		Model model = box.modelManager().model(this.name);
		//if (session.user() == null) return box.configuration().federationUrl() + "?authenticate-callback=" + callbackUrl;
		return session.browser().baseUrl() + (model != null ? "/permissions" : "/not-found") + "?model=" + Model.id(this.name, version) + "&username=" + user + "&callback=" + callbackUrl;
	}

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				ModelTemplate component = new ModelTemplate(box);
				component.user(ModelPage.this.user);
				component.model(Model.id(name, version));
				component.file(file);
				register(component);
				component.init();
				component.refresh();
			}
		};
	}
}