package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.WorkspaceCommands;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.ui.datasources.WorkspacesDatasource;
import io.intino.ime.box.ui.displays.rows.LanguageTableRow;
import io.intino.ime.box.util.WorkspaceHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Workspace;

public class WorkspacesTemplate extends AbstractWorkspacesTemplate<ImeBox> {

	public WorkspacesTemplate(ImeBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		initAddWorkspaceDialog();
		workspacesGroupSelector.onSelect(this::selectWorkspacesGroup);
		workspacesGroupSelector.select("allWorkspacesOption");
		workspacesCatalog.onOpenWorkspace(this::notifyOpeningWorkspace);
	}

	private void initAddWorkspaceDialog() {
		addWorkspaceDialog.onOpen(e -> refreshAddWorkspaceDialog());
		nameField.onChange(e -> DisplayHelper.checkWorkspaceName(nameField, this::translate, box()));
		createWorkspace.onExecute(e -> createWorkspace());
	}

	private void selectWorkspacesGroup(SelectionEvent event) {
		String selected = event.selection().isEmpty() ? "allWorkspacesOption" : (String) event.selection().getFirst();
		if (selected.equals("publicWorkspacesOption")) refreshWorkspaces("Public workspaces", new WorkspacesDatasource(box(), session(), false));
		else if (selected.equals("privateWorkspacesOption")) refreshWorkspaces("Private workspaces", new WorkspacesDatasource(box(), session(), true));
		else refreshWorkspaces("All workspaces", new WorkspacesDatasource(box(), session(), null));
	}

	private void refreshWorkspaces(String title, WorkspacesDatasource source) {
		workspacesCatalog.title(title);
		workspacesCatalog.source(source);
		workspacesCatalog.refresh();
	}

	private void notifyOpeningWorkspace(Workspace workspace) {
		bodyBlock.hide();
		openingWorkspaceMessage.value(String.format(translate("Opening %s"), workspace.title()));
		openingWorkspaceBlock.show();
	}

	private void refreshAddWorkspaceDialog() {
		nameField.value(WorkspaceHelper.proposeName());
		languageField.valueProvider(l -> ((Language)l).id());
		languageField.source(new LanguagesDatasource(box(), session()));
		languageTable.onAddItem(this::refreshLanguage);
	}

	private void refreshLanguage(AddItemEvent event) {
		Language language = event.item();
		LanguageTableRow row = event.component();
		row.ltNameItem.name.value(language.name());
		row.ltVersionItem.version.value(language.version());
		row.ltOwnerItem.owner.value(language.owner());
	}

	private void createWorkspace() {
		if (!DisplayHelper.checkWorkspaceName(nameField, this::translate, box())) return;
		if (!DisplayHelper.check(titleField, this::translate)) return;
		if (!DisplayHelper.check(languageField)) {
			notifyUser("Language field is required", UserMessage.Type.Warning);
			return;
		}
		addWorkspaceDialog.close();
		String languageId = ((Language)languageField.selection().getFirst()).id();
		Workspace workspace = box().commands(WorkspaceCommands.class).create(nameField.value(), titleField.value(), languageId, DisplayHelper.user(session()), username());
		notifyOpeningWorkspace(workspace);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.workspaceUrl(session(), workspace)), 600);
		workspacesCatalog.refresh();
	}

}