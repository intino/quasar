package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.WorkspaceCommands;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.util.WorkspaceHelper;
import io.intino.ime.model.Workspace;

import java.util.function.Consumer;

public class CloneWorkspaceEditor extends AbstractCloneWorkspaceEditor<ImeBox> {
	private Workspace workspace;
	private Mode mode = Mode.Large;
	private Consumer<Workspace> cloneListener;

	public CloneWorkspaceEditor(ImeBox box) {
		super(box);
	}

	public void workspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public enum Mode { Small, Large }
	public void mode(Mode mode) {
		this.mode = mode;
	}

	public void onClone(Consumer<Workspace> listener) {
		this.cloneListener = listener;
	}

	@Override
	public void init() {
		super.init();
		initDialog();
	}

	@Override
	public void refresh() {
		super.refresh();
		largeIcon.visible(mode == Mode.Large);
		smallIcon.visible(mode == Mode.Small);
	}

	private void initDialog() {
		workspaceDialog.onOpen(e -> refreshDialog());
		cloneWorkspace.onExecute(e -> cloneWorkspace());
		workspaceNameField.onChange(e -> DisplayHelper.checkWorkspaceName(workspaceNameField, this::translate, box()));
	}

	private void refreshDialog() {
		workspaceDialog.title(String.format(translate("Clone %s"), workspace.title()));
		workspaceNameField.value(WorkspaceHelper.proposeName());
		workspaceTitleField.value(String.format(translate("%s Copy"), workspace.title()));
	}

	private void cloneWorkspace() {
		if (!DisplayHelper.checkWorkspaceName(workspaceNameField, this::translate, box())) return;
		if (!DisplayHelper.check(workspaceTitleField, this::translate)) return;
		workspaceDialog.close();
		Workspace newWorkspace = box().commands(WorkspaceCommands.class).clone(workspace, workspaceNameField.value(), workspaceTitleField.value(), username());
		cloneListener.accept(newWorkspace);
	}

}