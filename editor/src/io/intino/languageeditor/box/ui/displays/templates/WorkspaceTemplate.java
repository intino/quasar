package io.intino.languageeditor.box.ui.displays.templates;

import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.languageeditor.box.LanguageEditorBox;
import io.intino.languageeditor.box.commands.WorkspaceCommands;
import io.intino.languageeditor.box.schemas.IntinoFileBrowserItem;
import io.intino.languageeditor.box.ui.DisplayHelper;
import io.intino.languageeditor.box.ui.displays.IntinoDslEditor;
import io.intino.languageeditor.box.ui.displays.IntinoFileBrowser;
import io.intino.languageeditor.box.util.Formatters;
import io.intino.languageeditor.box.workspaces.Workspace;
import io.intino.languageeditor.box.workspaces.WorkspaceContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkspaceTemplate extends AbstractWorkspaceTemplate<LanguageEditorBox> {
	private Workspace workspace;
	private WorkspaceContainer.File selectedFile;
	private WorkspaceContainer.File selectedNewFile;
	private WorkspaceContainer workspaceContainer;
	private boolean selectedFileIsModified = false;

	public WorkspaceTemplate(LanguageEditorBox box) {
		super(box);
	}

	public void workspace(String name) {
		this.workspace = box().workspaceManager().workspace(name);
	}

	@Override
	public void init() {
		super.init();
		initFileBrowser();
		initFileEditor();
		continueWithoutSavingFile.onExecute(e -> {
			fileModifiedDialog.close();
			selectedFileIsModified = false;
			open(selectedNewFile);
		});
	}

	@Override
	public void refresh() {
		super.refresh();
		workspaceContainer = box().workspaceManager().workspaceContainer(workspace);
		refreshHeader();
		refreshFileBrowser();
		refreshFileEditor();
	}

	private void refreshFileEditor() {
		boolean validFile = selectedFile != null && !selectedFile.content().isDirectory();
		fileNotSelectedBlock.visible(!validFile);
		fileSelectedBlock.visible(validFile);
		filename.value(validFile ? selectedFile.name() : "");
		refreshFileEditorToolbar();
		if (!validFile) return;
		IntinoDslEditor display = intinoDslEditor.display();
		display.file(selectedFile.name(), selectedFile.extension(), selectedFile.content(), selectedFile.language());
		display.refresh();
	}

	private void refreshFileEditorToolbar() {
		fileModifiedMessage.visible(selectedFile != null && selectedFileIsModified);
		saveFile.readonly(selectedFile == null || !selectedFileIsModified);
	}

	private void refreshHeader() {
		header.title(workspace.title());
		header.description(Formatters.countMessage(workspaceContainer.files().size(), "file", "files", language()));
		header.refresh();
	}

	private void initFileBrowser() {
		initFileBrowserToolbar();
		IntinoFileBrowser browser = new IntinoFileBrowser(box());
		browser.onOpen(this::open);
		intinoFileBrowser.display(browser);
	}

	private void initFileEditor() {
		initFileEditorToolbar();
		IntinoDslEditor editor = new IntinoDslEditor(box());
		intinoDslEditor.display(editor);
		editor.onFileModified(e -> {
			if (selectedFileIsModified) return;
			selectedFileIsModified = true;
			fileSavedMessage.visible(false);
			refreshFileEditorToolbar();
		});
		editor.onSaveFile(this::saveFile);
	}

	private void refreshFileBrowser() {
		removeFileTrigger.readonly(selectedFile == null);
		IntinoFileBrowser browser = intinoFileBrowser.display();
		browser.items(fileBrowserItems());
		browser.refresh();
	}

	private List<IntinoFileBrowserItem> fileBrowserItems() {
		List<WorkspaceContainer.File> files = workspaceContainer.files();
		Map<String, IntinoFileBrowserItem> items = new HashMap<>();
		files.forEach(f -> register(f, items));
		return new ArrayList<>(items.values());
	}

	private void register(WorkspaceContainer.File file, Map<String, IntinoFileBrowserItem> items) {
		List<String> parents = file.parents();
		if (!items.containsKey(file.name())) items.put(file.name(), new IntinoFileBrowserItem().name(file.name()).type(file.content().isFile() ? IntinoFileBrowserItem.Type.File : IntinoFileBrowserItem.Type.Folder).isRoot(parents.isEmpty()));
		for (int i = 0; i < parents.size(); i++) {
			register(parents.get(i), i > 0 ? parents.get(i-1) : null, items);
			if (i == parents.size()-1) register(file, parents.get(i), i == 0, items);
		}
	}

	private void register(String directory, String parent, Map<String, IntinoFileBrowserItem> items) {
		if (!items.containsKey(directory)) items.put(directory, new IntinoFileBrowserItem().name(directory).type(IntinoFileBrowserItem.Type.Folder).isRoot(parent == null));
		if (parent != null && !items.get(parent).children().contains(directory)) items.get(parent).children().add(directory);
	}

	private void register(WorkspaceContainer.File file, String parent, boolean isRoot, Map<String, IntinoFileBrowserItem> items) {
		if (!items.containsKey(parent)) items.put(parent, new IntinoFileBrowserItem().name(parent).type(IntinoFileBrowserItem.Type.Folder).isRoot(isRoot));
		if (file != null) items.get(parent).children().add(file.name());
	}

	private void open(String name) {
		open(workspaceContainer.file(name));
	}

	private void open(WorkspaceContainer.File file) {
		if (selectedFileIsModified) {
			selectedNewFile = file;
			fileModifiedDialog.open();
			return;
		}
		selectedFile = file;
		refresh();
	}

	private void initFileBrowserToolbar() {
		addFiles.onExecute(e -> addFiles());
		fileField.onChange(e -> addFiles.readonly(e.value() == null || ((String)e.value()).isEmpty()));
		fileField.onEnterPress(e -> addFiles());
		addFilesDialog.onOpen(e -> refreshAddFilesDialog());
		folderField.onChange(e -> addFolder.readonly(e.value() == null || ((String)e.value()).isEmpty()));
		folderField.onEnterPress(e -> addFolder());
		addFolder.onExecute(e -> addFolder());
		addFolderDialog.onOpen(e -> refreshAddFolderDialog());
		removeFileTrigger.onExecute(e -> removeFile());
	}

	private void initFileEditorToolbar() {
		saveFile.onExecute(e -> intinoDslEditor.<IntinoDslEditor>display().fireSavingProcess());
	}

	private void refreshAddFilesDialog() {
		fileField.value(null);
	}

	private void refreshAddFolderDialog() {
		folderField.value(null);
	}

	private void addFiles() {
		if (!DisplayHelper.check(fileField, this::translate)) return;
		addFilesDialog.close();
		WorkspaceContainer.File file = box().commands(WorkspaceCommands.class).create(workspace, fileField.value(), selectedFile, username());
		refresh();
		open(file);
	}

	private void addFolder() {
		if (!DisplayHelper.check(folderField, this::translate)) {
			return;
		}
		addFolderDialog.close();
		box().commands(WorkspaceCommands.class).create(workspace, folderField.value(), selectedFile, username());
		refresh();
	}

	private void saveFile(String content) {
		box().commands(WorkspaceCommands.class).save(workspace, selectedFile, content, username());
		fileSavedMessage.visible(true);
		DelayerUtil.execute(this, e -> fileSavedMessage.visible(false), 2000);
		selectedFileIsModified = false;
		refreshFileEditorToolbar();
	}

	private void removeFile() {
		box().commands(WorkspaceCommands.class).remove(workspace, selectedFile, username());
		selectedFile = null;
		refresh();
	}

}