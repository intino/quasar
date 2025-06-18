package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.components.FileEditable;
import io.intino.alexandria.ui.displays.events.ChangeEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.box.schemas.IntinoFileBrowserItem;
import io.quassar.editor.box.schemas.IntinoFileBrowserOperation;
import io.quassar.editor.box.schemas.IntinoFileBrowserOperationShortcut;
import io.quassar.editor.box.ui.displays.IntinoFileBrowser;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.box.util.*;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ModelBrowserTemplate extends AbstractModelBrowserTemplate<EditorBox> {
	private Model model;
	private String release;
	private LanguageTab tab;
	private ModelView view;
	private ModelContainer modelContainer;
	private File file;
	private Operation operation;
	private Consumer<IntinoFileBrowserItem> openListener;
	private Consumer<File> changeListener;
	private Consumer<File> removeListener;

	private enum Operation {CopyFile, AddFile, AddFolder, EditFilename}

	public ModelBrowserTemplate(EditorBox box) {
		super(box);
	}

	public void model(Model value) {
		this.model = value;
	}

	public void release(String value) {
		this.release = value;
	}

	public void tab(LanguageTab tab) {
		this.tab = tab;
	}

	public void view(ModelView view) {
		this.view = view;
	}

	public void modelContainer(ModelContainer value) {
		this.modelContainer = value;
	}

	public void file(File value) {
		this.file = value;
	}

	public void selection(String uri) {
		file = modelContainer.file(uri);
		fileBrowser.<IntinoFileBrowser>display().select(IntinoFileBrowserHelper.itemOf(file));
	}

	public void onOpen(Consumer<IntinoFileBrowserItem> listener) {
		this.openListener = listener;
	}

	public void onChange(Consumer<File> listener) {
		this.changeListener = listener;
	}

	public void onRemove(Consumer<File> listener) {
		this.removeListener = listener;
	}

	@Override
	public void didMount() {
		super.didMount();
		createBrowser();
		refresh();
	}

	@Override
	public void init() {
		super.init();
		operationsTrigger.onExecute(e -> openBrowserContextMenu());
		addFile.onExecute(e -> executeFileDialogOperation());
		fileDialog.onOpen(e -> refreshFileDialog());
		removeFileTrigger.onExecute(e -> removeFile());
		fileField.onChange(e -> addFile.readonly(e.value() == null || ((String) e.value()).isEmpty()));
		fileField.onEnterPress(e -> executeFileDialogOperation());
		addFileField.onChange(this::addFile);
		createBrowser();
	}

	@Override
	public void refresh() {
		super.refresh();
		if (model == null) return;
		IntinoFileBrowser browser = fileBrowser.display();
		browser.itemAddress(PathHelper.modelPath(model, release, tab, view) + "&file=:file");
		browser.items(IntinoFileBrowserHelper.fileBrowserItems(modelContainer.modelFiles()), true, true);
		browser.operations(operations());
		browser.selection(file != null ? IntinoFileBrowserHelper.itemOf(file) : null);
		browser.refresh();
		addFileField.readonly(!PermissionsHelper.canEdit(model, release, session(), box()));
		addFileField.allowedTypesByName(List.of(FileEditable.Type.Text.name(), FileEditable.Type.Zip.name(), Language.FileExtension));
	}

	private void createBrowser() {
		IntinoFileBrowser browser = new IntinoFileBrowser(box());
		browser.onOpen(f -> openListener.accept(f));
		browser.onExecuteOperation(this::execute);
		browser.onRename(this::rename);
		browser.onMove(this::move);
		fileBrowser.clear();
		fileBrowser.display(browser);
	}

	private void openBrowserContextMenu() {
		fileBrowser.<IntinoFileBrowser>display().openContextMenu(operations());
	}

	private List<IntinoFileBrowserOperation> operations() {
		if (model == null) return Collections.emptyList();
		return List.of(
				new IntinoFileBrowserOperation().name("Add model file...").shortcut(new IntinoFileBrowserOperationShortcut().ctrlKey(true).key("N")).enabled(PermissionsHelper.canEdit(model, release, session(), box())),
				new IntinoFileBrowserOperation().name("Add folder...").shortcut(new IntinoFileBrowserOperationShortcut().shiftKey(true).ctrlKey(true).key("N")).enabled(PermissionsHelper.canEdit(model, release, session(), box())),
				new IntinoFileBrowserOperation().name("Rename...").shortcut(new IntinoFileBrowserOperationShortcut().ctrlKey(true).key("R")).enabled(file != null && PermissionsHelper.canEdit(model, release, session(), box())),
				new IntinoFileBrowserOperation().name("Remove").shortcut(new IntinoFileBrowserOperationShortcut().ctrlKey(true).key("Backspace")).enabled(file != null && PermissionsHelper.canEdit(model, release, session(), box()))
		);
	}

	private void execute(String operation, IntinoFileBrowserItem target) {
		if (operation.equalsIgnoreCase("add model file...")) openFileDialog(Operation.AddFile);
		else if (operation.equalsIgnoreCase("add folder...")) openFileDialog(Operation.AddFolder);
		else if (operation.equalsIgnoreCase("rename...")) openFileDialog(Operation.EditFilename);
		else if (operation.equalsIgnoreCase("remove")) removeFileTrigger.launch();
	}

	private void openFileDialog(Operation operation) {
		this.operation = operation;
		fileDialog.open();
	}

	private void refreshFileDialog(Operation operation) {
		this.operation = operation;
		refreshFileDialog();
	}

	private void refreshFileDialog() {
		if (operation == null) return;
		fileDialog.title(fileDialogTitle());
		fileField.value(operation == Operation.EditFilename ? nameOf(file) : null);
	}

	private String fileDialogTitle() {
		if (operation == Operation.CopyFile) return translate("Copy model file");
		if (operation == Operation.AddFolder) return translate("Add model folder");
		if (operation == Operation.EditFilename) return translate("Edit model file name");
		return translate("Add model file");
	}

	private void executeFileDialogOperation() {
		if (!DisplayHelper.check(fileField, this::translate)) return;
		fileDialog.close();
		if (operation == Operation.AddFile) addFile();
		else if (operation == Operation.CopyFile) copyFile();
		else if (operation == Operation.EditFilename) rename();
		else if (operation == Operation.AddFolder) addFolder();
	}

	private void addFile(ChangeEvent event) {
		try {
			Resource value = event.value();
			if (value == null) return;
			if (ModelHelper.isZip(value.name()))
				box().commands(ModelCommands.class).addZip(model, ModelView.Model, value.stream(), file, username());
			else
				changeListener.accept(box().commands(ModelCommands.class).createFile(model, withExtension(value.name()), value.stream(), file, username()));
			refresh();
		} catch (IOException e) {
			Logger.error(e);
		} finally {
			addFileField.value((URL) null);
		}
	}

	private void addFile() {
		changeListener.accept(box().commands(ModelCommands.class).createFile(model, withExtension(fileField.value()), null, file, username()));
	}

	private void copyFile() {
		changeListener.accept(box().commands(ModelCommands.class).copy(model, fileField.value(), file, username()));
	}

	private void rename() {
		changeListener.accept(box().commands(ModelCommands.class).rename(model, withExtension(fileField.value()), file, username()));
	}

	private void addFolder() {
		changeListener.accept(box().commands(ModelCommands.class).createFolder(model, fileField.value(), file, username()));
	}

	private void removeFile() {
		box().commands(ModelCommands.class).remove(model, file, username());
		removeListener.accept(null);
	}

	private void rename(IntinoFileBrowserItem item, String newName) {
		File file = modelContainer.file(item.uri());
		if (!newName.endsWith(Language.FileExtension)) newName += Language.FileExtension;
		changeListener.accept(box().commands(ModelCommands.class).rename(model, newName, file, username()));
	}

	private void move(IntinoFileBrowserItem item, IntinoFileBrowserItem directoryItem) {
		File file = modelContainer.file(item.uri());
		File directory = directoryItem != null ? modelContainer.file(directoryItem.uri()) : null;
		changeListener.accept(box().commands(ModelCommands.class).move(model, file, directory, username()));
	}

	private String nameOf(File file) {
		if (file == null) return null;
		if (!file.name().contains(".")) return file.name();
		return file.name().substring(0, file.name().lastIndexOf("."));
	}

	private String withExtension(String name) {
		String extension = Language.FileExtension;
		return name.endsWith(extension) ? name : name + extension;
	}

}