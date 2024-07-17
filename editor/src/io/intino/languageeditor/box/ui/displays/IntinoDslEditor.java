package io.intino.languageeditor.box.ui.displays;

import io.intino.alexandria.logger.Logger;
import io.intino.languageeditor.box.LanguageEditorBox;
import io.intino.languageeditor.box.schemas.IntinoDslEditorFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

public class IntinoDslEditor extends AbstractIntinoDslEditor<LanguageEditorBox> {
	private String name;
	private File content;
	private String language;
	private Consumer<Boolean> fileModifiedListener;
	private Consumer<String> saveFileListener;

	public IntinoDslEditor(LanguageEditorBox box) {
		super(box);
	}

	public void file(String name, File content, String language) {
		this.name = name;
		this.content = content;
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
		return new IntinoDslEditorFile().name(name).content(content()).language(language);
	}

	private String content() {
		try {
			return Files.readString(content.toPath());
		} catch (IOException e) {
			Logger.error(e);
			return "";
		}
	}

}