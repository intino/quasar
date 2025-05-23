package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.ChangeEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.box.schemas.IntinoFileBrowserItem;
import io.quassar.editor.box.schemas.IntinoFileBrowserOperation;
import io.quassar.editor.box.schemas.IntinoFileBrowserOperationShortcut;
import io.quassar.editor.box.ui.displays.IntinoFileBrowser;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.box.util.*;
import io.quassar.editor.model.Model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ResourcesBrowserTemplate extends AbstractResourcesBrowserTemplate<EditorBox> {
	private Model model;
	private String release;
	private ModelView view;
	private ModelContainer modelContainer;
	private io.quassar.editor.box.models.File file;
	private Operation operation;
	private Consumer<IntinoFileBrowserItem> openListener;
	private Consumer<io.quassar.editor.box.models.File> changeListener;
	private Consumer<io.quassar.editor.box.models.File> removeListener;
	private Resource uploadedFile;


	private enum Operation { CopyFile, AddFile, AddFolder, EditFilename;}
	public ResourcesBrowserTemplate(EditorBox box) {
		super(box);
	}

	public void model(Model value) {
		this.model = value;
	}

	public void release(String value) {
		this.release = value;
	}

	public void view(ModelView view) {
		this.view = view;
	}

	public void modelContainer(ModelContainer value) {
		this.modelContainer = value;
	}

	public void file(io.quassar.editor.box.models.File value) {
		this.file = value;
	}

	public void selection(String uri) {
		file = modelContainer.file(uri);
		fileBrowser.<IntinoFileBrowser>display().select(IntinoFileBrowserHelper.itemOf(file));
	}

	public void onOpen(Consumer<IntinoFileBrowserItem> listener) {
		this.openListener = listener;
	}

	public void onChange(Consumer<io.quassar.editor.box.models.File> listener) {
		this.changeListener = listener;
	}

	public void onRemove(Consumer<io.quassar.editor.box.models.File> listener) {
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
		addFileField.onChange(this::addFile);
		createBrowser();
	}

	@Override
	public void refresh() {
		super.refresh();
		if (model == null) return;
		IntinoFileBrowser browser = fileBrowser.display();
		browser.itemAddress(PathHelper.modelPath(model, release, view) + "&file=:file");
		browser.rootItem(io.quassar.editor.box.models.File.ResourcesDirectory);
		browser.items(IntinoFileBrowserHelper.fileBrowserItems(modelContainer.resourceFiles()), false, true);
		browser.operations(operations());
		browser.selection(file != null ? IntinoFileBrowserHelper.itemOf(file) : null);
		browser.refresh();
		addFileField.readonly(!PermissionsHelper.canEdit(model, release, session()));
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
			new IntinoFileBrowserOperation().name("Add file...").shortcut(new IntinoFileBrowserOperationShortcut().ctrlKey(true).key("N")).enabled(PermissionsHelper.canEdit(model, release, session())),
			new IntinoFileBrowserOperation().name("Add folder...").shortcut(new IntinoFileBrowserOperationShortcut().shiftKey(true).ctrlKey(true).key("N")).enabled(PermissionsHelper.canEdit(model, release, session())),
			new IntinoFileBrowserOperation().name("Rename...").shortcut(new IntinoFileBrowserOperationShortcut().ctrlKey(true).key("R")).enabled(file != null && PermissionsHelper.canEdit(model, release, session())),
			new IntinoFileBrowserOperation().name("Remove").shortcut(new IntinoFileBrowserOperationShortcut().ctrlKey(true).key("Backspace")).enabled(file != null && PermissionsHelper.canEdit(model, release, session()))
		);
	}

	private void execute(String operation, IntinoFileBrowserItem target) {
		if (operation.equalsIgnoreCase("add file...")) openFileDialog(Operation.AddFile);
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
		fileDialogSelector.select("newFileOption");
		selectorBlock.visible(operation == Operation.AddFile);
		fileField.value(operation == Operation.EditFilename ? file.name() : null);
	}

	private String fileDialogTitle() {
		if (operation == Operation.CopyFile) return translate("Copy file");
		if (operation == Operation.AddFolder) return translate("Add folder");
		if (operation == Operation.EditFilename) return translate("Edit file name");
		return translate("Add file");
	}

	private void executeFileDialogOperation() {
		if (!isUploadOperation() && !DisplayHelper.check(fileField, this::translate)) return;
		if (isUploadOperation() && uploadedFile == null) {
			notifyUser(translate("Select file to upload"), UserMessage.Type.Warning);
			return;
		}
		fileDialog.close();
		if (operation == Operation.AddFile && fileDialogSelector.selection().getFirst().equals("newFileOption")) addFile();
		else if (operation == Operation.AddFile) uploadFile();
		else if (operation == Operation.CopyFile) copyFile();
		else if (operation == Operation.EditFilename) rename();
		else if (operation == Operation.AddFolder) addFolder();
	}

	private boolean isUploadOperation() {
		return operation == Operation.AddFile && fileDialogSelector.selection().getFirst().equals("uploadFileOption");
	}

	private void addFile(ChangeEvent event) {
		try {
			Resource value = event.value();
			if (value == null) return;
			if (ModelHelper.isZip(value.name()) && !ModelHelper.isArchetype(value.name())) box().commands(ModelCommands.class).addZip(model, ModelView.Resources, value.stream(), file, username());
			else changeListener.accept(box().commands(ModelCommands.class).createFile(model, nameOf(value.name()), value.stream(), file, username()));
			refresh();
		} catch (IOException e) {
			Logger.error(e);
		}
		finally {
			addFileField.value((URL) null);
		}
	}

	private void addFile() {
		changeListener.accept(box().commands(ModelCommands.class).createFile(model, nameOf(fileField.value()), new ByteArrayInputStream(new byte[0]), file, username()));
	}

	private void uploadFile() {
		try {
			changeListener.accept(box().commands(ModelCommands.class).createFile(model, nameOf(uploadedFile.name()), uploadedFile.stream(), file, username()));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void copyFile() {
		changeListener.accept(box().commands(ModelCommands.class).copy(model, nameOf(fileField.value()), file, username()));
	}

	private void rename() {
		changeListener.accept(box().commands(ModelCommands.class).rename(model, fileField.value(), file, username()));
	}

	private void addFolder() {
		changeListener.accept(box().commands(ModelCommands.class).createFolder(model, nameOf(fileField.value()), file, username()));
	}

	private void removeFile() {
		box().commands(ModelCommands.class).remove(model, file, username());
		removeListener.accept(null);
	}

	private void rename(IntinoFileBrowserItem item, String newName) {
		io.quassar.editor.box.models.File file = modelContainer.file(item.uri());
		changeListener.accept(box().commands(ModelCommands.class).rename(model, newName, file, username()));
	}

	private void move(IntinoFileBrowserItem item, IntinoFileBrowserItem directoryItem) {
		io.quassar.editor.box.models.File file = modelContainer.file(item.uri());
		io.quassar.editor.box.models.File directory = directoryItem != null ? modelContainer.file(directoryItem.uri()) : null;
		changeListener.accept(box().commands(ModelCommands.class).move(model, file, directory, username()));
	}

	private String nameOf(String name) {
		return file == null ? io.quassar.editor.box.models.File.ResourcesDirectory + File.separator + name : name;
	}

}