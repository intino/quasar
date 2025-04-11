package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;

import java.util.function.Consumer;

public class LanguageToolsTemplate extends AbstractLanguageToolsTemplate<EditorBox> {
	private Language language;
	private String release;
	private Consumer<LanguageRelease> createVersionListener;

	public LanguageToolsTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void release(String release) {
		this.release = release;
	}

	public void onCreateVersion(Consumer<LanguageRelease> listener) {
		this.createVersionListener = listener;
	}

	@Override
	public void init() {
		super.init();
		createVersion.onExecute(e -> createVersion());
	}

	@Override
	public void refresh() {
		super.refresh();
		LanguageRelease languageRelease = language.release(release);
		selectVersionBlock.visible(release == null);
		versionBlock.visible(release != null && languageRelease != null);
		versionNotCreatedBlock.visible(release != null && languageRelease == null);
		if (!versionBlock.isVisible()) return;
		// TODO 
	}

	private void createVersion() {
		notifyUser("Creating version...", UserMessage.Type.Loading);
		createVersion.readonly(true);
		createVersionListener.accept(box().commands(LanguageCommands.class).createRelease(language, release, username()));
		createVersion.readonly(false);
		hideUserNotification();
	}

}