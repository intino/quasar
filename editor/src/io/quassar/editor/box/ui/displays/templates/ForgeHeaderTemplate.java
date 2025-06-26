package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.services.push.User;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.languages.LanguageManager;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LogoSize;
import io.quassar.editor.model.Model;

public class ForgeHeaderTemplate extends AbstractForgeHeaderTemplate<EditorBox> {
	private Language language;
	private String release;
	private Model model;
	private boolean appInForge;

	public ForgeHeaderTemplate(EditorBox box) {
		super(box);
	}

	public void model(Model value) {
		this.model = value;
	}

	@Override
	public void init() {
		super.init();
		login.onExecute(e -> {
			session().add("callback", session().browser().requestUrl());
			if (box().authService() == null) notifier.redirect(PathHelper.loginUrl(session()));
			else notifier.redirect(session().login(session().browser().baseUrl()));
		});
	}

	@Override
	public void refresh() {
		super.refresh();
		Language language = model != null ? box().languageManager().get(model) : null;
		User loggedUser = session().user();
		logo.value(LanguageHelper.logo(language, LogoSize.S100, box()));
		homeLink.site(PathHelper.homeUrl(session()));
		aboutLink.site(PathHelper.aboutUrl(session()));
		projectsLink.site(PathHelper.homeUrl(session()));
		forgeTitle.value("%s DSL forge".formatted(language != null ? language.key() : ""));
		login.visible(loggedUser == null);
		user.visible(loggedUser != null);
	}

}