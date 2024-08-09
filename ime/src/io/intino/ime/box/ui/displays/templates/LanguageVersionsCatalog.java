package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.exceptions.*;
import io.intino.alexandria.ui.displays.Component;
import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.ime.box.*;
import io.intino.ime.box.commands.LanguageCommands;
import io.intino.ime.box.schemas.*;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.datasources.LanguageVersionsDatasource;
import io.intino.ime.box.ui.displays.rows.LanguageVersionsTableRow;
import io.intino.ime.box.ui.displays.templates.AbstractLanguageVersionsCatalog;
import io.intino.ime.model.Language;
import io.intino.ime.model.Workspace;

public class LanguageVersionsCatalog extends AbstractLanguageVersionsCatalog<ImeBox> {
	private Workspace workspace;

	public LanguageVersionsCatalog(ImeBox box) {
		super(box);
	}

	public void workspace(Workspace workspace) {
		this.workspace = workspace;
		this.languageVersionsTable.source(new LanguageVersionsDatasource(box(), session(), workspace.name()));
	}

	@Override
	public void init() {
		super.init();
		languageVersionsTable.onAddItem(this::refresh);
		createVersionDialog.onOpen(e -> refreshCreateVersionDialog());
		createVersion.onExecute(e -> createVersion());
	}

	@Override
	public void refresh() {
		super.refresh();
		languageVersionsTable.reload();
	}

	private void refresh(AddItemEvent event) {
		refresh(event.item(), event.component());
	}

	private void refresh(Language language, LanguageVersionsTableRow row) {
		row.lvtVersionItem.version.value(language.version());
		row.lvtAccessTypeItem.accessType.value(translate(language.isPublic() ? "Public" : "Private"));
		row.lvtUpdateDateItem.updateDate.value(language.createDate());
		row.lvtOperationsItem.publishVersion.visible(language.isPrivate());
		row.lvtOperationsItem.publishVersion.onExecute(e -> publishVersion(language, row));
		row.lvtOperationsItem.unpublishVersion.visible(language.isPublic());
		row.lvtOperationsItem.unpublishVersion.onExecute(e -> unpublishVersion(language, row));
	}

	private void createVersion() {
		if (!DisplayHelper.check(versionField, this::translate)) return;
		createVersionDialog.close();
		box().commands(LanguageCommands.class).create(workspace, versionField.value(), username());
		refresh();
	}

	private void publishVersion(Language language, LanguageVersionsTableRow row) {
		language = box().commands(LanguageCommands.class).publish(language, username());
		refresh(language, row);
	}

	private void unpublishVersion(Language language, LanguageVersionsTableRow row) {
		language = box().commands(LanguageCommands.class).unPublish(language, username());
		refresh(language, row);
	}

	private void refreshCreateVersionDialog() {
		versionField.value(null);
	}

}