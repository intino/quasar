package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.ArtifactoryHelper;
import io.quassar.editor.model.Language;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class DependencyTemplate extends AbstractDependencyTemplate<EditorBox> {
	private Language language;
	private String release;
	private File file;

	public DependencyTemplate(EditorBox box) {
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
	public void refresh() {
		super.refresh();
		String extension = FilenameUtils.getExtension(file.getName());
		title.value(translate("Reader for %s").formatted(file.getName().replace("." + extension, "")));
		contentField.value("<pre style='margin:0'>" + dependency().replace("<", "&lt;").replace("\t", "<span style='margin-right:10px'>&nbsp;</span>") + "</pre>");
		contentLink.text(dependency());
		repositoryField.value("<pre style='margin:0'>" + repository().replace("<", "&lt;").replace("\t", "<span style='margin-right:10px'>&nbsp;</span>") + "</pre>");
		repositoryLink.text(repository());
	}

	private String dependency() {
		return ArtifactoryHelper.dependency(language, release, file);
	}

	private String repository() {
		return ArtifactoryHelper.repository(box().configuration().url());
	}

}