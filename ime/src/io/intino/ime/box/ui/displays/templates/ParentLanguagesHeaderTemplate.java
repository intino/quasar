package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.model.Language;

import java.util.function.Consumer;

public class ParentLanguagesHeaderTemplate extends AbstractParentLanguagesHeaderTemplate<ImeBox> {
	private Language language;
	private Consumer<String> searchListener;

	public ParentLanguagesHeaderTemplate(ImeBox box) {
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
		logo.value(LanguageHelper.logo(language, box()));
		title.value(String.format(translate("Languages using %s"), language.name()));
		logoLink.path(PathHelper.languagePath(language));
		languageHome.path(PathHelper.languagePath(language));
	}

}