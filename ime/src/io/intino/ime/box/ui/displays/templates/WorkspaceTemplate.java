package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.WorkspaceCommands;
import io.intino.ime.box.schemas.IntinoFileBrowserItem;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.displays.IntinoDslEditor;
import io.intino.ime.box.ui.displays.IntinoFileBrowser;
import io.intino.ime.box.util.Formatters;
import io.intino.ime.box.workspaces.WorkspaceContainer;
import io.intino.ime.model.Workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkspaceTemplate extends AbstractWorkspaceTemplate<ImeBox> {
	private String user;
	private Workspace workspace;
	private WorkspaceContainer.File selectedFile;
	private WorkspaceContainer.File selectedNewFile;
	private WorkspaceContainer workspaceContainer;
	private boolean selectedFileIsModified = false;
	private boolean openNewFile = false;
	private FileDialogOperation fileDialogOperation;

	private enum FileDialogOperation { CopyFile, AddFile, AddFolder }

	public WorkspaceTemplate(ImeBox box) {
		super(box);
	}

	public void user(String user) {
		this.user = user;
	}

	public void workspace(String name) {
		try {
			this.workspace = box().workspaceManager().workspace(name);
			workspaceContainer = box().workspaceManager().workspaceContainer(workspace);
			if (workspace.language() == null) return;
			box().languageProvider().get(workspace.language());
		} catch (Throwable e) {
			Logger.error(e);
		}
	}

	public void file(String file) {
		if (file == null) return;
		this.selectedFile = workspaceContainer.file(file);
	}

	@Override
	public void init() {
		super.init();
		initFileBrowser();
		initFileEditor();
		initFileModifiedDialog();
	}

	private void initFileModifiedDialog() {
		fileModifiedDialog.onOpen(e -> fileModifiedDialog.title(selectedFile.name()));
		cancelSavingFile.onExecute(e -> {
			fileModifiedDialog.close();
			intinoFileBrowser.<IntinoFileBrowser>display().select(itemOf(selectedFile));
		});
		continueSavingFile.onExecute(e -> {
			fileModifiedDialog.close();
			openNewFile = true;
			intinoDslEditor.<IntinoDslEditor>display().fireSavingProcess();
		});
		continueWithoutSavingFile.onExecute(e -> {
			fileModifiedDialog.close();
			selectedFileIsModified = false;
			open(selectedNewFile);
		});
	}

	@Override
	public void refresh() {
		super.refresh();
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
		display.workspace(workspace);
		display.file(selectedFile.name(), selectedFile.extension(), selectedFile.content(), selectedFile.language());
		display.refresh();
	}

	private void refreshFileEditorToolbar() {
		fileModifiedMessage.visible(selectedFile != null && selectedFileIsModified);
		saveFile.readonly(selectedFile == null || !selectedFileIsModified);
	}

	private void refreshHeader() {
		header.workspace(workspace);
		header.title(workspace.title());
		header.description(Formatters.countMessage(workspaceContainer.files().stream().filter(f -> !f.content().isDirectory()).count(), "file", "files", language()));
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
		copyFileTrigger.readonly(selectedFile == null);
		removeFileTrigger.readonly(selectedFile == null);
		IntinoFileBrowser browser = intinoFileBrowser.display();
		browser.itemAddress(PathHelper.workspacePath(session(), workspace) + "?file=:file");
		browser.items(fileBrowserItems());
		if (selectedFile != null) browser.select(itemOf(selectedFile));
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
		if (!items.containsKey(file.name())) items.put(file.name(), itemOf(file));
		for (int i = 0; i < parents.size(); i++) {
			register(parents.get(i), i > 0 ? parents.get(i - 1) : null, items);
			if (i == parents.size() - 1) register(file, parents.get(i), i == 0, items);
		}
	}

	private void register(String directory, String parent, Map<String, IntinoFileBrowserItem> items) {
		if (!items.containsKey(directory))
			items.put(directory, itemOf(directory, IntinoFileBrowserItem.Type.Folder, parent == null));
		if (parent != null && !items.get(parent).children().contains(directory))
			items.get(parent).children().add(directory);
	}

	private void register(WorkspaceContainer.File file, String parent, boolean isRoot, Map<String, IntinoFileBrowserItem> items) {
		if (!items.containsKey(parent))
			items.put(parent, itemOf(parent, IntinoFileBrowserItem.Type.Folder, isRoot));
		if (file != null) items.get(parent).children().add(file.name());
	}

	private IntinoFileBrowserItem itemOf(WorkspaceContainer.File file) {
		IntinoFileBrowserItem.Type type = file.content().isFile() ? IntinoFileBrowserItem.Type.File : IntinoFileBrowserItem.Type.Folder;
		return itemOf(file.name(), type, file.parents().isEmpty());
	}

	private IntinoFileBrowserItem itemOf(String name, IntinoFileBrowserItem.Type type, boolean isRoot) {
		return new IntinoFileBrowserItem().name(name).type(type).isRoot(isRoot);
	}

	private void open(String name) {
		open(workspaceContainer.file(name));
	}

	private void open(WorkspaceContainer.File file) {
		openNewFile = false;
		if (selectedFileIsModified) {
			selectedNewFile = file;
			fileModifiedDialog.open();
			return;
		}
		selectedFile = file;
		refresh();
	}

	private void initFileBrowserToolbar() {
		copyFileTrigger.onOpen(e -> refreshFileDialog(FileDialogOperation.CopyFile));
		addFileTrigger.onOpen(e -> refreshFileDialog(FileDialogOperation.AddFile));
		addFolderTrigger.onOpen(e -> refreshFileDialog(FileDialogOperation.AddFolder));
		addFile.onExecute(e -> executeFileDialogOperation());
		fileField.onChange(e -> addFile.readonly(e.value() == null || ((String) e.value()).isEmpty()));
		fileField.onEnterPress(e -> executeFileDialogOperation());
		fileDialog.onOpen(e -> refreshFileDialog());
		removeFileTrigger.onExecute(e -> removeFile());
	}

	private void initFileEditorToolbar() {
		saveFile.onExecute(e -> intinoDslEditor.<IntinoDslEditor>display().fireSavingProcess());
	}

	private void refreshFileDialog(FileDialogOperation operation) {
		fileDialogOperation = operation;
		refreshFileDialog();
	}

	private void refreshFileDialog() {
		if (fileDialogOperation == null) return;
		fileDialog.title(fileDialogTitle());
		fileField.value(null);
	}

	private String fileDialogTitle() {
		if (fileDialogOperation == FileDialogOperation.CopyFile) return translate("Copy file");
		if (fileDialogOperation == FileDialogOperation.AddFolder) return translate("Add folder");
		return translate("Add file");
	}

	private void executeFileDialogOperation() {
		if (!DisplayHelper.check(fileField, this::translate)) return;
		fileDialog.close();
		if (fileDialogOperation == FileDialogOperation.AddFile) addFile();
		else if (fileDialogOperation == FileDialogOperation.CopyFile) copyFile();
		else if (fileDialogOperation == FileDialogOperation.AddFolder) addFolder();
	}

	private void copyFile() {
		WorkspaceContainer.File file = box().commands(WorkspaceCommands.class).copy(workspace, fileField.value(), selectedFile, username());
		reload();
		open(file);
	}

	private void addFile() {
		WorkspaceContainer.File file = box().commands(WorkspaceCommands.class).create(workspace, fileField.value(), selectedFile, username());
		reload();
		open(file);
	}

	private void addFolder() {
		box().commands(WorkspaceCommands.class).create(workspace, fileField.value(), selectedFile, username());
		reload();
	}

	private void saveFile(String content) {
		box().commands(WorkspaceCommands.class).save(workspace, selectedFile, content, username());
		fileSavedMessage.visible(true);
		DelayerUtil.execute(this, e -> fileSavedMessage.visible(false), 2000);
		selectedFileIsModified = false;
		if (openNewFile) open(selectedNewFile);
		else refreshFileEditorToolbar();
	}

	private void removeFile() {
		box().commands(WorkspaceCommands.class).remove(workspace, selectedFile, username());
		selectedFile = null;
		reload();
	}

	private void reload() {
		workspaceContainer = box().workspaceManager().workspaceContainer(workspace);
		refresh();
	}

}