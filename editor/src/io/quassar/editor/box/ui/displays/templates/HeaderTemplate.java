package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.services.push.User;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

public class HeaderTemplate extends AbstractHeaderTemplate<EditorBox> {
	private Language language;
	private Model model;

	public HeaderTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void model(Model model) {
		this.model = model;
	}

	@Override
	public void refresh() {
		super.refresh();
		User loggedUser = session().user();
		refreshLanguage();
		login.visible(loggedUser == null);
		user.visible(loggedUser != null);
		notLoggedBlock.visible(loggedUser == null);
	}

	private void refreshLanguage() {
		languageSeparator.visible(language != null);
		languageTitleText.visible(language != null && model == null);
		if (languageTitleText.isVisible()) languageTitleText.value(language.name());
		languageTitleLink.visible(language != null && model != null);
		if (!languageTitleLink.isVisible()) return;
		languageTitleLink.title(language.name());
		languageTitleLink.address(path -> PathHelper.languagePath(path, language, null));
	}

}