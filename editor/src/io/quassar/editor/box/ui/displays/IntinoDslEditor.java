package io.quassar.editor.box.ui.displays;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.schemas.IntinoDslEditorFile;
import io.quassar.editor.box.schemas.IntinoDslEditorSetup;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.util.function.Consumer;

public class IntinoDslEditor extends AbstractIntinoDslEditor<EditorBox> {
	private Model model;
	private String version;
	private String name;
	private String uri;
	private String extension;
	private String language;
	private Consumer<Boolean> fileModifiedListener;
	private Consumer<String> saveFileListener;

	public IntinoDslEditor(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void version(String version) {
		this.version = version;
	}

	public void file(String name, String uri, String extension, String language) {
		this.name = name;
		this.uri = uri;
		this.extension = extension;
		this.language = language;
	}

	public void onFileModified(Consumer<Boolean> listener) {
		this.fileModifiedListener = listener;
	}

	public void onSaveFile(Consumer<String> listener) {
		this.saveFileListener = listener;
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

	@Override
	public void refresh() {
		super.refresh();
		if (model == null) return;
		notifier.setup(info());
		notifier.refresh(file());
	}

	private IntinoDslEditorSetup info() {
		return new IntinoDslEditorSetup().dslName(Language.nameOf(model.language())).modelName(model.name()).modelVersion(version);
	}

	private IntinoDslEditorFile file() {
		return new IntinoDslEditorFile().name(name).uri(uri).extension(extension).content(content()).language(language);
	}

	private String content() {
		return box().modelManager().content(model, version, uri);
	}

}