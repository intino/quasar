package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.LanguageCommands;
import io.intino.ime.box.commands.WorkspaceCommands;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.util.WorkspaceHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.LanguageInfo;
import io.intino.ime.model.Workspace;

import java.util.function.Consumer;

public class WorkspaceHeaderTemplate extends AbstractWorkspaceHeaderTemplate<ImeBox> {
	private Workspace workspace;
	private String _title;
	private String _description = null;
	private Consumer<Workspace> openWorkspaceListener;

	public WorkspaceHeaderTemplate(ImeBox box) {
		super(box);
	}

	public void workspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public void title(String title) {
		this._title = title;
	}

	public void description(String value) {
		this._description = value;
	}

	public void onOpenWorkspace(Consumer<Workspace> listener) {
		this.openWorkspaceListener = listener;
	}

	@Override
	public void init() {
		super.init();
		cancel.onExecute(e -> hideTitleEditor());
		save.onExecute(e -> saveTitle());
		titleField.onEnterPress(e -> saveTitle());
		workspaces.visible(session().user() != null);
		user.visible(session().user() != null);
		versionsDialog.onOpen(e -> refreshLanguageVersionsDialog());
		settingsEditor.onSaveTitle(this::updateTitle);
		cloneWorkspaceEditor.onClone(this::open);
		removeWorkspace.onExecute(e -> removeWorkspace());
	}

	private void refreshLanguageVersionsDialog() {
		versionsCatalog.workspace(workspace);
		versionsCatalog.refresh();
	}

	@Override
	public void refresh() {
		super.refresh();
		titleLink.title(_title);
		titleLink.onExecute(e -> showTitleEditor());
		description.value(_description);
		if (session().user() == null) return;
		Language language = WorkspaceHelper.language(workspace, box());
		languageVersions.visible(language.level() != LanguageInfo.Level.L1);
		workspaces.path(PathHelper.workspacesPath(session()));
		myWorkspaces.path(PathHelper.workspacesPath(session()));
		settingsEditor.workspace(workspace);
		settingsEditor.mode(WorkspaceSettingsEditor.Mode.Large);
		settingsEditor.refresh();
		removeWorkspace.visible(!workspace.isTemporal());
		cloneWorkspaceEditor.workspace(workspace);
		cloneWorkspaceEditor.mode(CloneWorkspaceEditor.Mode.Large);
		cloneWorkspaceEditor.refresh();
	}

	private void open(Workspace workspace) {
		openWorkspaceListener.accept(workspace);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.workspaceUrl(session(), workspace)), 600);
	}

	private void showTitleEditor() {
		titleField.value(_title);
		titleLink.visible(false);
		titleEditor.visible(true);
		titleField.focus();
	}

	private void hideTitleEditor() {
		titleLink.visible(true);
		titleEditor.visible(false);
	}

	private void saveTitle() {
		_title = titleField.value();
		box().commands(WorkspaceCommands.class).saveTitle(workspace, _title, username());
		hideTitleEditor();
		refresh();
	}

	private void updateTitle(String title) {
		_title = title;
		refresh();
	}

	private void removeWorkspace() {
		box().commands(WorkspaceCommands.class).remove(workspace, username());
		notifier.redirect(PathHelper.homeUrl(session()));
	}


}