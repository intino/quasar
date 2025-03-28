package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.box.schemas.IntinoFileBrowserItem;
import io.quassar.editor.box.ui.displays.IntinoFileBrowser;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.IntinoFileBrowserHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Model;

import java.util.function.Consumer;

public class ModelBrowserTemplate extends AbstractModelBrowserTemplate<EditorBox> {
	private Model model;
	private String release;
	private ModelContainer modelContainer;
	private ModelContainer.File file;
	private Operation operation;
	private Consumer<IntinoFileBrowserItem> openListener;
	private Consumer<ModelContainer.File> changeListener;

	private enum Operation { CopyFile, AddFile, AddFolder, EditFilename }

	public ModelBrowserTemplate(EditorBox box) {
		super(box);
	}

	public void model(Model value) {
		this.model = value;
	}

	public void release(String value) {
		this.release = value;
	}

	public void modelContainer(ModelContainer value) {
		this.modelContainer = value;
	}

	public void file(ModelContainer.File value) {
		this.file = value;
	}

	public void onOpen(Consumer<IntinoFileBrowserItem> listener) {
		this.openListener = listener;
	}

	public void onChange(Consumer<ModelContainer.File> listener) {
		this.changeListener = listener;
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
		operationsDialog.onOpen(e -> refreshOperationsDialog());
		addFileTrigger.onOpen(e -> refreshFileDialog(Operation.AddFile));
		addFolderTrigger.onOpen(e -> refreshFileDialog(Operation.AddFolder));
		editFilenameTrigger.onOpen(e -> refreshFileDialog(Operation.EditFilename));
		addFile.onExecute(e -> executeFileDialogOperation());
		fileDialog.onOpen(e -> refreshFileDialog());
		removeFileTrigger.onExecute(e -> removeFile());
		fileField.onChange(e -> addFile.readonly(e.value() == null || ((String) e.value()).isEmpty()));
		fileField.onEnterPress(e -> executeFileDialogOperation());
		createBrowser();
	}

	@Override
	public void refresh() {
		super.refresh();
		IntinoFileBrowser browser = fileBrowser.display();
		browser.itemAddress(PathHelper.modelPath(model, release) + "&file=:file");
		browser.items(IntinoFileBrowserHelper.fileBrowserItems(modelContainer.modelFiles()));
		browser.select(file != null ? IntinoFileBrowserHelper.itemOf(file) : null);
		browser.refresh();
	}

	private void createBrowser() {
		IntinoFileBrowser browser = new IntinoFileBrowser(box());
		browser.onOpen(f -> openListener.accept(f));
		browser.onRename(this::rename);
		browser.onMove(this::move);
		fileBrowser.clear();
		fileBrowser.display(browser);
	}

	private void refreshOperationsDialog() {
		addFileTrigger.readonly(!PermissionsHelper.canEdit(model, release));
		addFolderTrigger.readonly(!PermissionsHelper.canEdit(model, release));
		editFilenameTrigger.readonly(file == null || !PermissionsHelper.canEdit(model, release));
		removeFileTrigger.readonly(file == null || !PermissionsHelper.canEdit(model, release));
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

	private void addFile() {
		changeListener.accept(box().commands(ModelCommands.class).createFile(model, withExtension(fileField.value()), null, file, username()));
		operationsDialog.close();
	}

	private void copyFile() {
		changeListener.accept(box().commands(ModelCommands.class).copy(model, fileField.value(), file, username()));
		operationsDialog.close();
	}

	private void rename() {
		changeListener.accept(box().commands(ModelCommands.class).rename(model, withExtension(fileField.value() ), file, username()));
		operationsDialog.close();
	}

	private void addFolder() {
		changeListener.accept(box().commands(ModelCommands.class).createFolder(model, fileField.value(), file, username()));
		operationsDialog.close();
	}

	private void removeFile() {
		box().commands(ModelCommands.class).remove(model, file, username());
		changeListener.accept(null);
		operationsDialog.close();
	}

	private void rename(IntinoFileBrowserItem item, String newName) {
		ModelContainer.File file = modelContainer.file(item.uri());
		changeListener.accept(box().commands(ModelCommands.class).rename(model, newName, file, username()));
		operationsDialog.close();
	}

	private void move(IntinoFileBrowserItem item, IntinoFileBrowserItem directoryItem) {
		ModelContainer.File file = modelContainer.file(item.uri());
		ModelContainer.File directory = modelContainer.file(directoryItem.uri());
		changeListener.accept(box().commands(ModelCommands.class).move(model, file, directory, username()));
		operationsDialog.close();
	}

	private String nameOf(ModelContainer.File file) {
		return file.name().substring(0, file.name().lastIndexOf("."));
	}

	private String withExtension(String name) {
		return name + "." + box().languageManager().get(model.language()).fileExtension();
	}

}