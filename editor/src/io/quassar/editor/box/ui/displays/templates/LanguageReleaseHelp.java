package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;

public class LanguageReleaseHelp extends AbstractLanguageReleaseHelp<EditorBox> {
	private Language language;
	private LanguageRelease release;

	public LanguageReleaseHelp(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void release(LanguageRelease release) {
		this.release = release;
	}

	@Override
	public void refresh() {
		super.refresh();
		titleLink.title(release.version());
		titleLink.site(PathHelper.languageReleaseHelp(language, release));
	}
}