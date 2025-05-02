package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.model.Language;

import java.io.File;

public class ModelReaderTemplate extends AbstractModelReaderTemplate<EditorBox> {
	private Language language;
	private String release;
	private File reader;

	public ModelReaderTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void release(String release) {
		this.release = release;
	}

	public void reader(File reader) {
		this.reader = reader;
	}

	@Override
	public void init() {
		super.init();
		titleLink.onExecute(e -> DisplayHelper.uiFile(filename(), reader));
	}

	@Override
	public void refresh() {
		super.refresh();
		titleLink.title(reader.getName().replace(".zip", "").replace("reader-", ""));
	}

	private String filename() {
		return language.name() + "-" + release + "-" + reader.getName();
	}
}