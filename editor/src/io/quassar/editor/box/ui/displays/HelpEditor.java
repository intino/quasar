package io.quassar.editor.box.ui.displays;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.User;

public class HelpEditor extends AbstractHelpEditor<EditorBox> {
	private Language language;
	private String release;

	public HelpEditor(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void release(String release) {
		this.release = release;
	}

	public void update(String value) {
		box().commands(LanguageCommands.class).saveHelp(language, release, value, username());
	}

	@Override
	public void refresh() {
		super.refresh();
		if (language == null || release == null) return;
		notifier.refresh(box().languageManager().loadHelp(language, release));
	}

	private String username() {
		return session() != null && session().user() != null ? session().user().username() : User.Anonymous;
	}

}