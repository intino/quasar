package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.model.Language;

import java.util.function.Consumer;

public class LanguageModelsHeaderTemplate extends AbstractLanguageModelsHeaderTemplate<ImeBox> {
	private Language language;
	private Consumer<String> searchListener;

	public LanguageModelsHeaderTemplate(ImeBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void onSearch(Consumer<String> listener) {
		this.searchListener = listener;
	}

	@Override
	public void init() {
		super.init();
		searchField.onChange(e -> searchListener.accept(e.value()));
		searchField.onEnterPress(e -> searchListener.accept(e.value()));
	}

	@Override
	public void refresh() {
		super.refresh();
		notLoggedToolbar.visible(session().user() == null);
		user.visible(session().user() != null);
		logo.value(LanguageHelper.logo(language, box()));
		title.value(String.format(translate("Models using %s"), language.name()));
		logoLink.path(PathHelper.languagePath(language));
		userHome.path(PathHelper.userHomePath(session()));
		languageHome.path(PathHelper.languagePath(language));
	}
}