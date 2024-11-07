package io.intino.ime.box.ui.pages;

import io.intino.alexandria.ui.services.push.User;
import io.intino.ime.box.I18n;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.ViewMode;
import io.intino.ime.box.ui.displays.templates.HomeTemplate;
import io.intino.ime.box.ui.displays.templates.ModelTemplate;
import io.intino.ime.model.Model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ModelPage extends AbstractModelPage {
	public String user;
	public String id;
	public String file;

	@Override
	public boolean hasPermissions() {
		if (PathHelper.PublicUser.equals(user)) return true;
		Model model = box.modelManager().model(id);
		if (model == null) return false;
		if (model.isPublic()) return true;
		User loggedUser = session.user();
		return loggedUser != null && model.owner().equals(loggedUser.username());
	}

	@Override
	public String redirectUrl() {
		String callbackUrl = URLEncoder.encode(session.browser().requestUrl(), StandardCharsets.UTF_8);
		Model model = box.modelManager().model(id);
		return model != null ? PathHelper.permissionsUrl(model, callbackUrl, session) : PathHelper.notFoundUrl(I18n.translate("Model", session.discoverLanguage()), session);
	}

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				DisplayHelper.updateViewMode(ViewMode.Models, session);
				HomeTemplate component = new HomeTemplate(box);
				register(component);
				component.init();
				component.openModel(id, file);
			}
		};
	}

}