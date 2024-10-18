package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.datasources.LanguageReleasesDatasource;
import io.intino.ime.box.ui.displays.rows.LanguageReleasesTableRow;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Release;

import java.util.function.Consumer;

public class LanguageReleasesCatalog extends AbstractLanguageReleasesCatalog<ImeBox> {
	private Language language;
	private Consumer<Release> createLanguageListener;
	private Consumer<Release> createModelListener;
	private boolean readonly = false;

	public LanguageReleasesCatalog(ImeBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
		this.languageReleasesTable.source(new LanguageReleasesDatasource(box(), session(), language.name()));
	}

	public void onCreateLanguage(Consumer<Release> listener) {
		this.createLanguageListener = listener;
	}

	public void onCreateModel(Consumer<Release> listener) {
		this.createModelListener = listener;
	}

	public void readonly(boolean readonly) {
		this.readonly = readonly;
	}

	@Override
	public void init() {
		super.init();
		languageReleasesTable.onAddItem(this::refresh);
	}

	@Override
	public void refresh() {
		super.refresh();
		languageReleasesTable.reload();
	}

	private void refresh(AddItemEvent event) {
		refresh(event.item(), event.component());
	}

	private void refresh(Release release, LanguageReleasesTableRow row) {
		row.lrcVersionItem.version.value(release.version());
		row.lrcLevelItem.level.value(translate(release.level().label()));
		row.lrcOperationsItem.createLanguage.visible(!readonly && LanguageHelper.canCreateLanguage(language, release, user()));
		row.lrcOperationsItem.createLanguage.onExecute(e -> createLanguageListener.accept(release));
		row.lrcOperationsItem.addModelWithRelease.visible(!readonly && ModelHelper.canAddModel(release));
		row.lrcOperationsItem.addModelWithRelease.onExecute(e -> createModelListener.accept(release));
	}
}