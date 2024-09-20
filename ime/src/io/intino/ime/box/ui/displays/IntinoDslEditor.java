package io.intino.ime.box.ui.displays;

import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.schemas.IntinoDslEditorFile;
import io.intino.ime.model.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

public class IntinoDslEditor extends AbstractIntinoDslEditor<ImeBox> {
	private Model model;
	private String name;
	private String uri;
	private String extension;
	private String language;
	private Consumer<Boolean> fileModifiedListener;
	private Consumer<String> saveFileListener;

	public IntinoDslEditor(ImeBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
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
		notifier.refresh(file());
	}

	private IntinoDslEditorFile file() {
		return new IntinoDslEditorFile().model(model.id()).name(name).uri(uri).extension(extension).content(content()).language(language);
	}

	private String content() {
		return box().modelManager().content(model, uri);
	}

}