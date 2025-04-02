package io.quassar.editor.box.ui.displays;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.schemas.IntinoDslEditorFile;
import io.quassar.editor.box.schemas.IntinoDslEditorFilePosition;
import io.quassar.editor.box.schemas.IntinoDslEditorSetup;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.FilePosition;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.util.function.Consumer;

public class IntinoDslEditor extends AbstractIntinoDslEditor<EditorBox> {
	private Model model;
	private String release;
	private String name;
	private String uri;
	private String extension;
	private String language;
	private FilePosition position;
	private Consumer<Boolean> fileModifiedListener;
	private Consumer<String> saveFileListener;
	private Consumer<Boolean> buildListener;
	private boolean reloadRequired;

	public IntinoDslEditor(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String value) {
		this.release = value;
	}

	public void file(String name, String uri, String extension, String language, FilePosition position) {
		this.reloadRequired = this.uri == null || !this.uri.equals(uri);
		this.name = name;
		this.uri = uri;
		this.extension = extension;
		this.language = language;
		this.position = position;
	}

	public void onFileModified(Consumer<Boolean> listener) {
		this.fileModifiedListener = listener;
	}

	public void onSaveFile(Consumer<String> listener) {
		this.saveFileListener = listener;
	}

	public void onBuild(Consumer<Boolean> listener) {
		this.buildListener = listener;
	}

	public void fireSavingProcess() {
		notifier.receiveContent();
	}

	public void fileModified() {
		if (fileModifiedListener == null) return;
		fileModifiedListener.accept(true);
	}

	public void fileContent(String content) {
		if (saveFileListener == null) return;
		saveFileListener.accept(content);
	}

	public void executeCommand(String name) {
		if (name.equalsIgnoreCase("build")) buildListener.accept(true);
	}

	@Override
	public void refresh() {
		super.refresh();
		if (model == null) return;
		if (reloadRequired) reload();
		else refreshPosition();
	}

	private void reload() {
		notifier.setup(info());
		notifier.refresh(file());
	}

	private void refreshPosition() {
		if (position == null) return;
		notifier.refreshPosition(positionOf(position));
	}

	private IntinoDslEditorSetup info() {
		return new IntinoDslEditorSetup().dslName(Language.nameOf(model.language())).modelName(model.name()).modelRelease(release).readonly(!PermissionsHelper.canEdit(model, release, session()));
	}

	private IntinoDslEditorFile file() {
		return new IntinoDslEditorFile().name(name).uri(uri).extension(extension).content(content()).language(language).position(positionOf(position));
	}

	private IntinoDslEditorFilePosition positionOf(FilePosition position) {
		if (position == null) return null;
		return new IntinoDslEditorFilePosition().line(position.line()).column(position.column());
	}

	private String content() {
		return box().modelManager().content(language(), model, release, uri);
	}

	private Language language() {
		return box().languageManager().get(model.language());
	}

}