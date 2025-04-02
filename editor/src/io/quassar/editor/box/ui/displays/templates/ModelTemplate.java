package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.builderservice.schemas.Message;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.ExecutionResult;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.box.schemas.IntinoFileBrowserItem;
import io.quassar.editor.box.ui.displays.IntinoDslEditor;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.SessionHelper;
import io.quassar.editor.model.FilePosition;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.util.*;

public class ModelTemplate extends AbstractModelTemplate<EditorBox> {
	private Model model;
	private String release;
	private ModelView selectedView;
	private ModelContainer.File selectedFile;
	private FilePosition selectedPosition;
	private ModelContainer.File selectedNewFile;
	private ModelContainer modelContainer;
	private boolean selectedFileIsModified = false;
	private boolean openNewFile = false;

	public ModelTemplate(EditorBox box) {
		super(box);
	}

	public void open(String language, String model, String release, String view, String file, String position) {
		this.model = box().modelManager().get(language, model);
		this.release = release != null ? release : Model.DraftRelease;
		this.selectedView = view != null ? ModelView.from(view) : SessionHelper.modelView(session());
		this.modelContainer = this.model != null ? box().modelManager().modelContainer(box().languageManager().get(language), this.model, this.release) : null;
		this.selectedFile = file != null && modelContainer != null ? modelContainer.file(file) : null;
		this.selectedPosition = position != null ? FilePosition.from(position) : null;
		refresh();
	}

	@Override
	public void didMount() {
		super.didMount();
		createFileEditor();
		refresh();
	}

	@Override
	public void init() {
		super.init();
		initHeader();
		initBrowsers();
		initFileEditor();
		initFileModifiedDialog();
		console.onClose(e -> consoleBlock.hide());
	}

	@Override
	public void refresh() {
		super.refresh();
		notFoundBlock.visible(model == null);
		refreshHeader();
		refreshContent();
	}

	private void refreshContent() {
		tabSelector.address(path -> PathHelper.modelViewPath(path, model, release));
		contentBlock.visible(model != null);
		if (!contentBlock.isVisible()) return;
		if (selectedFile != null) tabSelector.select(modelContainer.isResourceFile(selectedFile) ? "resources" : "model");
		else if (selectedView != null) tabSelector.select(selectedView.name().toLowerCase());
		else if (tabSelector.selection().isEmpty()) tabSelector.select("model");
		else tabSelector.select(tabSelector.selection().getFirst());
		refreshFileEditor();
	}

	private void initHeader() {
		headerStamp.onBuild(m -> build());
		headerStamp.onClone(m -> cloneModel());
		headerStamp.onPublish((m, e) -> updateConsole(e));
	}

	private void initBrowsers() {
		modelBrowserBlock.onInit(e -> initModelBrowserBlock());
		modelBrowserBlock.onShow(e -> refreshModelBrowserBlock());
		resourcesBrowserBlock.onInit(e -> initResourcesBrowserBlock());
		resourcesBrowserBlock.onShow(e -> refreshResourcesBrowserBlock());
	}

	private void initModelBrowserBlock() {
		modelBrowserStamp.onChange(this::reloadAndOpen);
		modelBrowserStamp.onOpen(this::open);
	}

	private void refreshModelBrowserBlock() {
		modelBrowserStamp.model(model);
		modelBrowserStamp.release(release);
		modelBrowserStamp.modelContainer(modelContainer);
		modelBrowserStamp.file(selectedFile);
		modelBrowserStamp.refresh();
	}

	private void initResourcesBrowserBlock() {
		resourcesBrowserStamp.onChange(this::reloadAndOpen);
		resourcesBrowserStamp.onOpen(this::open);
	}

	private void refreshResourcesBrowserBlock() {
		resourcesBrowserStamp.model(model);
		resourcesBrowserStamp.release(release);
		resourcesBrowserStamp.modelContainer(modelContainer);
		resourcesBrowserStamp.file(selectedFile);
		resourcesBrowserStamp.refresh();
	}

	private void initFileEditor() {
		createFileEditor();
	}

	private void createFileEditor() {
		IntinoDslEditor editor = new IntinoDslEditor(box());
		intinoDslEditor.clear();
		intinoDslEditor.display(editor);
		editor.onFileModified(e -> {
			if (selectedFileIsModified) return;
			selectedFileIsModified = true;
			fileSavedMessage.visible(false);
			refreshFileEditorToolbar();
		});
		editor.onSaveFile(this::saveFile);
		editor.onBuild(e -> build());
	}

	private void initFileModifiedDialog() {
		fileModifiedDialog.onOpen(e -> fileModifiedDialog.title(selectedFile.name()));
		cancelSavingFile.onExecute(e -> {
			fileModifiedDialog.close();
			refreshFileBrowser();
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

	private void refreshFileBrowser() {
		if (modelBrowserBlock.isVisible()) refreshModelBrowserBlock();
		if (resourcesBrowserBlock.isVisible()) refreshResourcesBrowserBlock();
	}

	private void saveFile(String content) {
		box().commands(ModelCommands.class).save(model, selectedFile, content, username());
		fileSavedMessage.visible(true);
		DelayerUtil.execute(this, e -> fileSavedMessage.visible(false), 2000);
		selectedFileIsModified = false;
		if (openNewFile) open(selectedNewFile);
		else refreshFileEditorToolbar();
	}

	private void refreshHeader() {
		headerStamp.model(model);
		headerStamp.release(release);
		headerStamp.file(selectedFile);
		headerStamp.refresh();
	}

	private void refreshFileEditor() {
		boolean validFile = selectedFile != null && !selectedFile.isDirectory();
		fileNotSelectedBlock.visible(!validFile);
		fileSelectedBlock.visible(validFile);
		filename.value(validFile ? withoutExtensionIfModelFile(selectedFile.name()) : "");
		refreshFileEditorToolbar();
		if (!validFile) return;
		createFileEditor();
		IntinoDslEditor display = intinoDslEditor.display();
		display.model(model);
		display.release(release);
		display.file(selectedFile.name(), selectedFile.uri(), selectedFile.extension(), selectedFile.language(), selectedPosition);
		display.refresh();
	}

	private String withoutExtensionIfModelFile(String name) {
		return Model.isResource(name) ? name : name.replace("." + box().languageManager().get(model.language()).fileExtension(), "");
	}

	private void open(IntinoFileBrowserItem item) {
		open(modelContainer.file(item.uri()));
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

	private void reload() {
		Language language = box().languageManager().get(model.language());
		modelContainer = box().modelManager().modelContainer(language, model, release);
		refresh();
	}

	private void refreshFileEditorToolbar() {
		//fileModifiedMessage.visible(selectedFile != null && selectedFileIsModified);
		//saveFile.readonly(selectedFile == null || !selectedFileIsModified);
	}

	private void updateConsole(ExecutionResult result) {
		consoleBlock.visible(!result.success());
		if (!consoleBlock.isVisible()) return;
		updateConsole(result.messages());
	}

	private void updateConsole(List<Message> messages) {
		console.model(model);
		console.release(release);
		console.messages(messages);
		console.refresh();
		console.show();
	}

	private void reloadAndOpen(ModelContainer.File file) {
		reload();
		if (file != null) open(file);
	}

	private void build() {
		notifyUser(translate("Building model..."), UserMessage.Type.Loading);
		ExecutionResult result = box().commands(ModelCommands.class).build(model, username());
		updateConsole(result);
		if (result.success()) notifyUser("Model built successfully", UserMessage.Type.Success);
		else hideUserNotification();
	}

	private void cloneModel() {
		notifyUser(translate("Cloning model..."), UserMessage.Type.Loading);
		Model newModel = box().commands(ModelCommands.class).clone(model, release, ModelHelper.proposeName(), username());
		notifier.dispatch(PathHelper.modelPath(newModel));
		hideUserNotification();
	}

}