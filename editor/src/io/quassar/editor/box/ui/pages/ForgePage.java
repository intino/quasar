package io.quassar.editor.box.ui.pages;

import io.intino.alexandria.ui.services.push.User;
import io.quassar.editor.box.I18n;
import io.quassar.editor.box.ui.displays.templates.HomeTemplate;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ForgePage extends AbstractForgePage {
	public String model;
	public String release;
	public String view;

	@Override
	public boolean hasPermissions() {
		Model model = box.modelManager().get(this.model);
		if (model == null) return false;
		if (model.isPublic()) return true;
		User loggedUser = session.user();
		return loggedUser != null && model.owner().equals(loggedUser.username());
	}

	@Override
	public String redirectUrl() {
		String callbackUrl = URLEncoder.encode(session.browser().requestUrl(), StandardCharsets.UTF_8);
		Model model = box.modelManager().get(this.model);
		return model != null ? PathHelper.permissionsUrl(model, callbackUrl, session) : PathHelper.notFoundUrl(I18n.translate("Model", session.discoverLanguage()), session);
	}

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				HomeTemplate component = new HomeTemplate(box);
				register(component);
				component.init();
			}
		};
	}
}