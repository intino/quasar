package io.quassar.editor.box.ui.pages;

import io.intino.alexandria.ui.services.push.User;
import io.quassar.editor.box.I18n;
import io.quassar.editor.box.ui.displays.templates.HomeTemplate;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LanguagePage extends AbstractLanguagePage {
	public String language;
	public String tab;
	public String view;

	@Override
	public boolean hasPermissions() {
		Language language = box.languageManager().get(this.language);
		if (language == null) return false;
		if (language.isPublic()) return true;
		User loggedUser = session.user();
		return loggedUser != null && box.languageManager().hasAccess(language, loggedUser.username());
	}

	@Override
	public String redirectUrl() {
		String callbackUrl = URLEncoder.encode(session.browser().requestUrl(), StandardCharsets.UTF_8);
		Language language = box.languageManager().get(this.language);
		return language != null ? PathHelper.permissionsUrl(language, callbackUrl, session) : PathHelper.notFoundUrl(I18n.translate("Language", session.discoverLanguage()), session);
	}

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		session.language("en");
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