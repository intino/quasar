package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.model.Language;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.patch.FileHeader;

import java.io.File;

public class DownloadTemplate extends AbstractDownloadTemplate<EditorBox> {
	private Language language;
	private String release;
	private File file;

	public DownloadTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void release(String release) {
		this.release = release;
	}

	public void file(File file) {
		this.file = file;
	}

	@Override
	public void init() {
		super.init();
		titleLink.onExecute(e -> DisplayHelper.uiFile(filename(), file));
	}

	@Override
	public void refresh() {
		super.refresh();
		String extension = FilenameUtils.getExtension(file.getName());
		titleLink.title(file.getName().replace("." + extension, ""));
	}

	private String filename() {
		String extension = FilenameUtils.getExtension(file.getName());
		return language.name() + "-" + file.getName().replace("." + extension, "") + "-" + release + "." + extension;
	}
}