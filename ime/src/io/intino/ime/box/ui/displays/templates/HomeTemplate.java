package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.util.Formatters;
import io.intino.ime.model.Workspace;

public class HomeTemplate extends AbstractHomeTemplate<ImeBox> {

	public HomeTemplate(ImeBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		header.onSearch(this::filter);
		languagesCatalog.onOpenWorkspace(this::notifyOpeningWorkspace);
		refresh();
	}

	@Override
	public void refresh() {
		super.refresh();
		header.refresh();
		LanguagesDatasource source = new LanguagesDatasource(box(), session());
		countLanguages.value(Formatters.countMessage(source.itemCount(), "language", "languages", language()));
		refreshWorkspaces(source);
	}

	private void refreshWorkspaces(LanguagesDatasource source) {
		languagesCatalog.source(source);
		languagesCatalog.refresh();
	}

	private void notifyOpeningWorkspace(Workspace workspace) {
		bodyBlock.hide();
		openingWorkspaceMessage.value(String.format(translate("Opening %s"), workspace.title()));
		searchingWorkspacesBlock.show();
	}

	private void filter(String condition) {
		languagesCatalog.filter(condition);
	}

}