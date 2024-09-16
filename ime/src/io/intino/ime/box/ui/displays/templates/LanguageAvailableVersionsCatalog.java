package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.datasources.LanguageVersionsDatasource;
import io.intino.ime.box.ui.displays.rows.LanguageAvailableVersionsTableRow;
import io.intino.ime.model.Language;

import java.util.function.Consumer;

public class LanguageAvailableVersionsCatalog extends AbstractLanguageAvailableVersionsCatalog<ImeBox> {
	private Language language;
	private Consumer<Language> createModelListener;

	public LanguageAvailableVersionsCatalog(ImeBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
		this.languageAvailableVersionsTable.source(new LanguageVersionsDatasource(box(), session(), language.name()));
	}

	public void onCreateModel(Consumer<Language> listener) {
		this.createModelListener = listener;
	}

	@Override
	public void init() {
		super.init();
		languageAvailableVersionsTable.onAddItem(this::refresh);
	}

	@Override
	public void refresh() {
		super.refresh();
		languageAvailableVersionsTable.reload();
	}

	private void refresh(AddItemEvent event) {
		refresh(event.item(), event.component());
	}

	private void refresh(Language language, LanguageAvailableVersionsTableRow row) {
		row.lavtVersionItem.version.value(language.version());
		row.lavtUpdateDateItem.updateDate.value(language.createDate());
		row.lavtOperationsItem.addModelWithVersion.onExecute(e -> createModel(language));
	}

	private void createModel(Language language) {
		createModelListener.accept(language);
	}
}