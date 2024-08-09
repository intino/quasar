package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.UserMessage;
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

import java.io.IOException;
import java.util.*;

public class WorkspaceTemplate extends AbstractWorkspaceTemplate<ImeBox> {
	private String user;
	private Workspace workspace;
	private WorkspaceContainer.File selectedFile;
	private WorkspaceContainer.File selectedNewFile;
	private WorkspaceContainer workspaceContainer;
	private boolean selectedFileIsModified = false;
	private boolean openNewFile = false;
	private FileDialogOperation fileDialogOperation;
	private Resource uploadedFile;

	private enum FileDialogOperation { CopyFile, AddFile, AddFolder, EditFilename }

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
		header.onOpenWorkspace(this::notifyOpeningWorkspace);
		initFileBrowser();
		initFileEditor();
		initFileModifiedDialog();
	}

	private void initFileModifiedDialog() {
		fileModifiedDialog.onOpen(e -> fileModifiedDialog.title(selectedFile.name()));
		cancelSavingFile.onExecute(e -> {
			fileModifiedDialog.close();
			intinoFileBrowser.<IntinoFileBrowser>display().select(itemOf(selectedFile));
			intinoFileBrowser.<IntinoFileBrowser>display().refresh();
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
		browser.onRename(this::rename);
		browser.onMove(this::move);
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
		editFilenameTrigger.readonly(selectedFile == null);
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
		if (!items.containsKey(file.name())) items.put(file.name(), itemOf(file).id(items.size()));
		for (int i = 0; i < parents.size(); i++) {
			register(parents.get(i), i > 0 ? parents.get(i - 1) : null, items);
			if (i == parents.size() - 1) register(file, parents.get(i), i == 0, items);
		}
	}

	private void register(String directory, String parent, Map<String, IntinoFileBrowserItem> items) {
		if (!items.containsKey(directory))
			items.put(directory, itemOf(directory, parent != null ? List.of(parent) : Collections.emptyList(), IntinoFileBrowserItem.Type.Folder, parent == null).id(items.size()));
		if (parent != null && !items.get(parent).children().contains(directory))
			items.get(parent).children().add(directory);
	}

	private void register(WorkspaceContainer.File file, String parent, boolean isRoot, Map<String, IntinoFileBrowserItem> items) {
		if (!items.containsKey(parent))
			items.put(parent, itemOf(parent, Collections.emptyList(), IntinoFileBrowserItem.Type.Folder, isRoot).id(items.size()));
		if (file != null) items.get(parent).children().add(file.name());
	}

	private IntinoFileBrowserItem itemOf(WorkspaceContainer.File file) {
		IntinoFileBrowserItem.Type type = file.content().isFile() ? IntinoFileBrowserItem.Type.File : IntinoFileBrowserItem.Type.Folder;
		return itemOf(file.name(), file.parents(), type, file.parents().isEmpty());
	}

	private IntinoFileBrowserItem itemOf(String name, List<String> parents, IntinoFileBrowserItem.Type type, boolean isRoot) {
		return new IntinoFileBrowserItem().name(name).parents(parents).type(type).isRoot(isRoot);
	}

	private void open(String name) {
		open(workspaceContainer.file(name));
	}

	private void rename(IntinoFileBrowserItem item, String newName) {
		WorkspaceContainer.File file = workspaceContainer.file(item.name());
		file = box().commands(WorkspaceCommands.class).rename(workspace, newName, file, username());
		reload();
		open(file);
	}

	private void move(IntinoFileBrowserItem item, IntinoFileBrowserItem directoryItem) {
		WorkspaceContainer.File file = workspaceContainer.file(item.name());
		WorkspaceContainer.File directory = workspaceContainer.file(directoryItem.name());
		file = box().commands(WorkspaceCommands.class).move(workspace, file, directory, username());
		reload();
		open(file);
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
		editFilenameTrigger.onOpen(e -> refreshFileDialog(FileDialogOperation.EditFilename));
		addFile.onExecute(e -> executeFileDialogOperation());
		fileUploadBlock.onInit(v -> fileUploadField.onChange(e -> {
			uploadedFile = e.value();
			addFile.readonly(uploadedFile == null);
		}));
		fileFieldBlock.onInit(v -> {
			fileField.onChange(e -> addFile.readonly(e.value() == null || ((String) e.value()).isEmpty()));
			fileField.onEnterPress(e -> executeFileDialogOperation());
		});
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
		fileDialogSelector.select("newFileOption");
		selectorBlock.visible(fileDialogOperation == FileDialogOperation.AddFile);
		fileField.value(fileDialogOperation == FileDialogOperation.EditFilename ? selectedFile.name() : null);
	}

	private String fileDialogTitle() {
		if (fileDialogOperation == FileDialogOperation.CopyFile) return translate("Copy file");
		if (fileDialogOperation == FileDialogOperation.AddFolder) return translate("Add folder");
		if (fileDialogOperation == FileDialogOperation.EditFilename) return translate("Edit name");
		return translate("Add file");
	}

	private void executeFileDialogOperation() {
		if (!isUploadOperation() && !DisplayHelper.check(fileField, this::translate)) return;
		if (isUploadOperation() && uploadedFile == null) {
			notifyUser(translate("Select file to upload"), UserMessage.Type.Warning);
			return;
		}
		fileDialog.close();
		if (fileDialogOperation == FileDialogOperation.AddFile && fileDialogSelector.selection().getFirst().equals("newFileOption")) addFile();
		else if (fileDialogOperation == FileDialogOperation.AddFile) uploadFile();
		else if (fileDialogOperation == FileDialogOperation.CopyFile) copyFile();
		else if (fileDialogOperation == FileDialogOperation.EditFilename) rename();
		else if (fileDialogOperation == FileDialogOperation.AddFolder) addFolder();
	}

	private boolean isUploadOperation() {
		return fileDialogOperation == FileDialogOperation.AddFile && fileDialogSelector.selection().get(0).equals("uploadFileOption");
	}

	private void addFile() {
		WorkspaceContainer.File file = box().commands(WorkspaceCommands.class).createFile(workspace, fileField.value(), "", selectedFile, username());
		reload();
		open(file);
	}

	private void copyFile() {
		WorkspaceContainer.File file = box().commands(WorkspaceCommands.class).copy(workspace, fileField.value(), selectedFile, username());
		reload();
		open(file);
	}

	private void uploadFile() {
		try {
			WorkspaceContainer.File file = box().commands(WorkspaceCommands.class).createFile(workspace, uploadedFile.name(), uploadedFile.readAsString(), selectedFile, username());
			reload();
			open(file);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void rename() {
		WorkspaceContainer.File file = box().commands(WorkspaceCommands.class).rename(workspace, fileField.value(), selectedFile, username());
		reload();
		open(file);
	}

	private void addFolder() {
		box().commands(WorkspaceCommands.class).createFolder(workspace, fileField.value(), selectedFile, username());
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

	private void notifyOpeningWorkspace(Workspace workspace) {
		bodyBlock.hide();
		openingWorkspaceMessage.value(String.format(translate("Opening %s"), workspace.title()));
		openingWorkspaceBlock.show();
	}

}