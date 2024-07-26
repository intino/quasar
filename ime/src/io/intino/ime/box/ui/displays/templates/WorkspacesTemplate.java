package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.WorkspaceCommands;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.OwnerWorkspacesDatasource;
import io.intino.ime.box.ui.datasources.SharedWorkspacesDatasource;
import io.intino.ime.box.ui.datasources.WorkspacesDatasource;
import io.intino.ime.box.util.Languages;
import io.intino.ime.box.util.WorkspaceHelper;
import io.intino.ime.box.workspaces.Workspace;

public class WorkspacesTemplate extends AbstractWorkspacesTemplate<ImeBox> {
	private String workspaceProposedName;

	public WorkspacesTemplate(ImeBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		initAddWorkspaceDialog();
		workspacesGroupSelector.onSelect(this::selectWorkspacesGroup);
		workspacesGroupSelector.select("ownerWorkspacesOption");
		workspacesCatalog.onOpenWorkspace(this::notifyOpeningWorkspace);
	}

	private void initAddWorkspaceDialog() {
		addWorkspaceDialog.onOpen(e -> refreshAddWorkspaceDialog());
		createWorkspace.onExecute(e -> createWorkspace());
	}

	private void selectWorkspacesGroup(SelectionEvent event) {
		String selected = event.selection().isEmpty() ? "ownerWorkspacesOption" : (String) event.selection().getFirst();
		if (selected.equals("sharedWorkspacesOption")) refreshWorkspaces("Shared with you", new SharedWorkspacesDatasource(box(), session()));
		else refreshWorkspaces("My workspaces", new OwnerWorkspacesDatasource(box(), session()));
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
		workspaceProposedName = WorkspaceHelper.proposeName(box());
		nameField.value(workspaceProposedName);
		dslField.clear();
		dslField.addAll(Languages.all());
		dslVersionField.value(null);
	}

	private void createWorkspace() {
		if (!DisplayHelper.check(titleField, this::translate)) return;
		if (!DisplayHelper.check(dslField)) {
			notifyUser("DSL field is required", UserMessage.Type.Warning);
			return;
		}
		if (!DisplayHelper.check(dslVersionField, this::translate)) return;
		addWorkspaceDialog.close();
		String dsl = dslField.selection().getFirst() + ":" + dslVersionField.value();
		Workspace workspace = box().commands(WorkspaceCommands.class).create(workspaceProposedName, titleField.value(), dsl, username());
		notifyOpeningWorkspace(workspace);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.workspacePath(session(), workspace)), 600);
		workspacesCatalog.refresh();
	}

}