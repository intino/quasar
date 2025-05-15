package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.MimeTypes;
import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.builderservice.schemas.Message;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.box.schemas.IntinoFileBrowserItem;
import io.quassar.editor.box.ui.displays.IntinoDslEditor;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.FilePosition;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ModelEditor extends AbstractModelEditor<EditorBox> {
	private Model model;
	private String release;
	private ModelView selectedView;
	private io.quassar.editor.box.models.File selectedFile;
	private FilePosition selectedPosition;
	private io.quassar.editor.box.models.File selectedNewFile;
	private ModelContainer modelContainer;
	private boolean selectedFileIsModified = false;
	private boolean openNewFile = false;
	private boolean showHelp = false;

	public ModelEditor(EditorBox box) {
		super(box);
	}

	public void model(Model model, String release) {
		this.model = model;
		this.release = release;
		this.modelContainer = this.model != null ? box().modelManager().modelContainer(this.model, this.release) : null;
	}

	public void view(ModelView view) {
		this.selectedView = view;
	}

	public void file(File file, FilePosition position) {
		this.selectedFile = file;
		this.selectedPosition = position;
	}

	public void showHelp(boolean value) {
		this.showHelp = value;
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
		//tabSelector.onSelect(this::updateSelectedBlock);
		helpDialog.onClose(e -> notifier.dispatch(PathHelper.modelPath(model, release)));
		helpDialog.onOpen(e -> refreshHelpDialog());
		console.onClose(e -> consoleBlock.hide());
		infoTrigger.onExecute(e -> headerStamp.openInfo());
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshHeader();
		languageNotLoadedBlock.visible(modelContainer == null);
		refreshContent();
		if (showHelp) helpDialog.open();
	}

	private void refreshContent() {
		tabSelector.address(path -> PathHelper.modelViewPath(path, model, release));
		contentBlock.visible(model != null && modelContainer != null);
		if (!contentBlock.isVisible()) return;
		if (selectedFile != null) tabSelector.select(selectedFile.isResource() ? "resources" : "model");
		else if (selectedView != null) tabSelector.select(selectedView.name().toLowerCase());
		else if (tabSelector.selection().isEmpty()) tabSelector.select("model");
		else tabSelector.select(tabSelector.selection().getFirst());
		refreshFile();
	}

	private void initHeader() {
		headerStamp.onCheck(m -> check());
		headerStamp.onClone(m -> cloneModel());
		headerStamp.onDeploy((m, e) -> updateConsole(e));
		headerStamp.onUpdateLanguageVersion(m -> reload());
	}

	private void initBrowsers() {
		modelBrowserBlock.onInit(e -> initModelBrowserBlock());
		modelBrowserBlock.onShow(e -> refreshModelBrowserBlock());
		resourcesBrowserBlock.onInit(e -> initResourcesBrowserBlock());
		resourcesBrowserBlock.onShow(e -> refreshResourcesBrowserBlock());
	}

	private void initModelBrowserBlock() {
		modelBrowserStamp.onChange(this::reloadAndOpen);
		modelBrowserStamp.onRemove(this::reloadAndUnselect);
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
		resourcesBrowserStamp.onRemove(this::reloadAndUnselect);
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

	private IntinoDslEditor createFileEditor() {
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
		editor.onBuild(e -> check());
		return editor;
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
		box().commands(ModelCommands.class).save(model, selectedFile, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), username());
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

	private void refreshFile() {
		boolean isDirectory = selectedFile != null && selectedFile.isDirectory();
		boolean textFile = selectedFile != null && !isDirectory && box().modelManager().isTextFile(model, release, selectedFile);
		fileNotSelectedBlock.visible(selectedFile == null || selectedFile.isDirectory());
		nonEditableFileBlock.visible(selectedFile != null && !isDirectory && !textFile);
		editableFileBlock.visible(textFile);
		filename.value(textFile ? withoutExtensionIfModelFile(selectedFile.name()) : "");
		refreshFileEditorToolbar();
		refreshEditableFileBlock();
		refreshNonEditableFileBlock();
	}

	private void refreshEditableFileBlock() {
		if (!editableFileBlock.isVisible()) return;
		IntinoDslEditor display = intinoDslEditor.display();
		if (display == null || !display.sameReleaseAndFile(release, selectedFile.uri())) display = createFileEditor();
		display.model(model);
		display.release(release);
		display.file(selectedFile.name(), selectedFile.uri(), selectedFile.extension(), selectedFile.language(), selectedPosition);
		display.refresh();
	}

	private void refreshNonEditableFileBlock() {
		if (!nonEditableFileBlock.isVisible()) return;
		fileField.value(new io.intino.alexandria.ui.File().filename(selectedFile.uri()).mimeType(MimeTypes.contentTypeOf(selectedFile.extension())).value(urlOf(PathHelper.fileUrl(model, release, selectedFile, session(), box()))));
	}

	private String withoutExtensionIfModelFile(String name) {
		return io.quassar.editor.box.models.File.isResource(name) ? name : name.replace(Language.FileExtension, "");
	}

	private void open(IntinoFileBrowserItem item) {
		open(modelContainer.file(item.uri()));
	}

	private void open(io.quassar.editor.box.models.File file) {
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
		io.quassar.editor.box.models.File file = modelContainer.file(item.name());
		file = box().commands(ModelCommands.class).rename(model, newName, file, username());
		reload();
		open(file);
	}

	private void move(IntinoFileBrowserItem item, IntinoFileBrowserItem directoryItem) {
		io.quassar.editor.box.models.File file = modelContainer.file(item.name());
		io.quassar.editor.box.models.File directory = modelContainer.file(directoryItem.name());
		file = box().commands(ModelCommands.class).move(model, file, directory, username());
		reload();
		open(file);
	}

	private void reload() {
		modelContainer = box().modelManager().modelContainer(model, release);
		refresh();
	}

	private void refreshFileEditorToolbar() {
		//fileModifiedMessage.visible(selectedFile != null && selectedFileIsModified);
		//saveFile.readonly(selectedFile == null || !selectedFileIsModified);
	}

	private void updateConsole(Command.ExecutionResult result) {
		consoleBlock.visible(!result.messages().isEmpty());
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

	private void reloadAndUnselect(io.quassar.editor.box.models.File file) {
		selectedFile = null;
		reload();
	}

	private void reloadAndOpen(io.quassar.editor.box.models.File file) {
		reload();
		if (file != null) open(file);
	}

	private void check() {
		notifyUser(translate("Checking model..."), UserMessage.Type.Loading);
		Command.ExecutionResult result = box().commands(ModelCommands.class).check(model, username());
		updateConsole(result);
		if (result.success()) notifyUser("Model checked successfully", UserMessage.Type.Success);
		else hideUserNotification();
	}

	private void cloneModel() {
		notifyUser(translate("Cloning model..."), UserMessage.Type.Loading);
		Model newModel = box().commands(ModelCommands.class).clone(model, release, ModelHelper.proposeName(), username());
		notifier.dispatch(PathHelper.modelPath(newModel));
		hideUserNotification();
	}

	private void refreshHelpDialog() {
		String help = box().languageManager().loadHelp(model.language());
		helpDialog.title("%s help".formatted(LanguageHelper.title(model.language())));
		helpStamp.content(help);
		helpStamp.refresh();
	}

	private URL urlOf(String url) {
		try {
			return new URI(url).toURL();
		} catch (URISyntaxException | MalformedURLException ignored) {
			return null;
		}
	}

	private void updateSelectedBlock(SelectionEvent event) {
		if (!model.isTemplate()) return;
		String selected = !event.selection().isEmpty() ? (String) event.selection().getFirst() : null;
		if (selected == null || selected.equalsIgnoreCase("model")) modelBrowserBlock.show();
		else resourcesBrowserBlock.show();
	}

}