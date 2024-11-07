package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.components.Layer;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.builderservice.schemas.Message;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.commands.Command.ExecutionResult;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.models.ModelContainer;
import io.intino.ime.box.schemas.IntinoFileBrowserItem;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.displays.IntinoDslEditor;
import io.intino.ime.box.ui.displays.IntinoFileBrowser;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Operation;

import java.io.IOException;
import java.util.*;

public class ModelTemplate extends AbstractModelTemplate<ImeBox> {
	private Model model;
	private ModelContainer.File selectedFile;
	private ModelContainer.File selectedNewFile;
	private ModelContainer modelContainer;
	private boolean selectedFileIsModified = false;
	private boolean openNewFile = false;
	private FileDialogOperation fileDialogOperation;
	private Resource uploadedFile;

	private enum FileDialogOperation { CopyFile, AddFile, AddFolder, EditFilename }

	public ModelTemplate(ImeBox box) {
		super(box);
	}

	public void model(String id) {
		try {
			this.model = box().modelManager().model(id);
			this.modelContainer = model != null ? box().modelManager().modelContainer(model) : null;
			if (model == null || model.modelingLanguage() == null) return;
			box().languageProvider().get(model.modelingLanguage());
		} catch (Throwable e) {
			Logger.error(e);
		}
	}

	public void file(String file) {
		if (file == null) return;
		this.selectedFile = modelContainer.file(file);
	}

	@Override
	public void init() {
		super.init();
		header.onOpenSearch(e -> openSearch());
		header.onOpenModel(this::open);
		header.onPublish(this::refreshConsole);
		header.onExecuteOperation(this::executeOperation);
		header.onOpenLanguage(this::open);
		console.onClose(e -> console.hide());
		initFileBrowser();
		initFileEditor();
		initFileModifiedDialog();
		workspaceDialog.onOpen(e -> refreshWorkspaceDialog());
		openSearchLayerTrigger.onOpen(e -> openSearch(e.layer()));
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

	private void refreshWorkspaceDialog() {
		removeFileTrigger.readonly(selectedFile == null);
		editFilenameTrigger.readonly(selectedFile == null);
	}

	@Override
	public void refresh() {
		super.refresh();
		if (model == null) {
			notifier.redirect(PathHelper.notFoundUrl(translate("Model"), session()));
			return;
		}
		refreshHeader();
		refreshFileBrowser();
		refreshFileEditor();
	}

	private void refreshHeader() {
		header.model(model);
		header.title(ModelHelper.label(model, language(), box()));
		header.refresh();
	}

	private void refreshFileBrowser() {
		IntinoFileBrowser browser = intinoFileBrowser.display();
		browser.itemAddress(PathHelper.modelPath(session(), model) + "?file=:file");
		browser.items(fileBrowserItems());
		if (selectedFile != null) browser.select(itemOf(selectedFile));
		browser.refresh();
	}

	private void refreshFileEditor() {
		boolean validFile = selectedFile != null && !selectedFile.isDirectory();
		fileNotSelectedBlock.visible(!validFile);
		fileSelectedBlock.visible(validFile);
		filename.value(validFile ? selectedFile.name() : "");
		refreshFileEditorToolbar();
		if (!validFile) return;
		IntinoDslEditor display = intinoDslEditor.display();
		display.model(model);
		display.file(selectedFile.name(), selectedFile.uri(), selectedFile.extension(), selectedFile.language());
		display.refresh();
	}

	private void refreshFileEditorToolbar() {
		//fileModifiedMessage.visible(selectedFile != null && selectedFileIsModified);
		//saveFile.readonly(selectedFile == null || !selectedFileIsModified);
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

	private List<IntinoFileBrowserItem> fileBrowserItems() {
		List<ModelContainer.File> files = modelContainer.files();
		Map<String, IntinoFileBrowserItem> items = new HashMap<>();
		files.forEach(f -> register(f, items));
		return new ArrayList<>(items.values());
	}

	private void register(ModelContainer.File file, Map<String, IntinoFileBrowserItem> items) {
		List<String> parents = file.parents();
		if (!items.containsKey(file.uri())) items.put(file.uri(), itemOf(file).id(items.size()));
		for (int i = 0; i < parents.size(); i++) {
			register(uri(parents, i), i > 0 ? uri(parents, i-1) : null, items);
			if (i == parents.size() - 1) register(file, uri(parents, i), i == 0, items);
		}
	}

	private String uri(List<String> parents, int i) {
		return String.join("/", parents.subList(0, i+1));
	}

	private void register(String directory, String parent, Map<String, IntinoFileBrowserItem> items) {
		if (!items.containsKey(directory))
			items.put(directory, itemOf(directory, parent != null ? List.of(parent) : Collections.emptyList(), IntinoFileBrowserItem.Type.Folder, parent == null).id(items.size()));
		if (parent != null && !items.get(parent).children().contains(directory))
			items.get(parent).children().add(directory);
	}

	private void register(ModelContainer.File file, String parent, boolean isRoot, Map<String, IntinoFileBrowserItem> items) {
		if (!items.containsKey(parent))
			items.put(parent, itemOf(parent, Collections.emptyList(), IntinoFileBrowserItem.Type.Folder, isRoot).id(items.size()));
		if (file != null && !items.get(parent).children().contains(file.uri())) items.get(parent).children().add(file.uri());
	}

	private IntinoFileBrowserItem itemOf(ModelContainer.File file) {
		IntinoFileBrowserItem.Type type = file.isDirectory() ? IntinoFileBrowserItem.Type.Folder : IntinoFileBrowserItem.Type.File;
		return itemOf(file.uri(), file.parents(), type, file.parents().isEmpty());
	}

	private IntinoFileBrowserItem itemOf(String uri, List<String> parents, IntinoFileBrowserItem.Type type, boolean isRoot) {
		return new IntinoFileBrowserItem().name(nameOf(uri)).uri(uri).parents(parents).type(type).isRoot(isRoot);
	}

	private String nameOf(String uri) {
		return uri.contains("/") ? uri.substring(uri.lastIndexOf("/")+1) : uri;
	}

	private void open(IntinoFileBrowserItem item) {
		open(modelContainer.file(item.uri()));
	}

	private void rename(IntinoFileBrowserItem item, String newName) {
		ModelContainer.File file = modelContainer.file(item.name());
		file = box().commands(ModelCommands.class).rename(model, newName, file, username());
		reload();
		open(file);
	}

	private void move(IntinoFileBrowserItem item, IntinoFileBrowserItem directoryItem) {
		ModelContainer.File file = modelContainer.file(item.name());
		ModelContainer.File directory = modelContainer.file(directoryItem.name());
		file = box().commands(ModelCommands.class).move(model, file, directory, username());
		reload();
		open(file);
	}

	private void open(ModelContainer.File file) {
		openNewFile = false;
		/*
		if (selectedFileIsModified) {
			selectedNewFile = file;
			fileModifiedDialog.open();
			return;
		}*/
		selectedFile = file;
		refresh();
	}

	private void initFileBrowserToolbar() {
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
		//saveFile.onExecute(e -> intinoDslEditor.<IntinoDslEditor>display().fireSavingProcess());
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
		ModelContainer.File file = box().commands(ModelCommands.class).createFile(model, fileField.value(), "", selectedFile, username());
		reload();
		open(file);
	}

	private void copyFile() {
		ModelContainer.File file = box().commands(ModelCommands.class).copy(model, fileField.value(), selectedFile, username());
		reload();
		open(file);
	}

	private void uploadFile() {
		try {
			ModelContainer.File file = box().commands(ModelCommands.class).createFile(model, uploadedFile.name(), uploadedFile.readAsString(), selectedFile, username());
			reload();
			open(file);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void rename() {
		ModelContainer.File file = box().commands(ModelCommands.class).rename(model, fileField.value(), selectedFile, username());
		reload();
		open(file);
	}

	private void addFolder() {
		box().commands(ModelCommands.class).createFolder(model, fileField.value(), selectedFile, username());
		reload();
	}

	private void saveFile(String content) {
		box().commands(ModelCommands.class).save(model, selectedFile, content, username());
		fileSavedMessage.visible(true);
		DelayerUtil.execute(this, e -> fileSavedMessage.visible(false), 2000);
		selectedFileIsModified = false;
		if (openNewFile) open(selectedNewFile);
		else refreshFileEditorToolbar();
	}

	private void removeFile() {
		box().commands(ModelCommands.class).remove(model, selectedFile, username());
		selectedFile = null;
		reload();
	}

	private void reload() {
		modelContainer = box().modelManager().modelContainer(model);
		refresh();
	}

	private void notifyOpening(Model model) {
		notifyOpening(ModelHelper.label(model, language(), box()));
	}

	private void notifyOpening(Language language) {
		notifyOpening(LanguageHelper.label(language, this::translate));
	}

	private void notifyOpening(String element) {
		bodyBlock.hide();
		openingModelMessage.value(String.format(translate("Opening %s"), element));
		openingModelBlock.show();
	}

	private void open(Language language) {
		notifyOpening(language);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.languageUrl(session(), language)), 600);
	}

	private void open(Model model) {
		notifyOpening(model);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
	}

	private void openSearch() {
		openSearchLayerTrigger.address(path -> PathHelper.searchPath());
		openSearchLayerTrigger.launch();
	}

	private void openSearch(Layer<?, ?> layer) {
		HomeTemplate template = new HomeTemplate(box());
		template.id(UUID.randomUUID().toString());
		layer.template(template);
		template.page(HomeTemplate.Page.Search);
		template.refresh();
	}

	private void executeOperation(Operation operation) {
		ExecutionResult result = box().commands(ModelCommands.class).execute(model, operation, username());
		refreshConsole(result.messages());
		if (result.success()) notifyUser(translate("Operation execution finished"), UserMessage.Type.Success);
		else notifyUser(translate("Errors detected during operation execution"), UserMessage.Type.Error);
	}

	private void refreshConsole(ExecutionResult result) {
		refreshConsole(result.messages());
	}

	private void refreshConsole(List<Message> messages) {
		console.messages(messages);
		console.refresh();
		console.show();
	}

}