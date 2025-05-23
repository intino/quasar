package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.Display;
import io.intino.alexandria.ui.displays.UserMessage;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.ui.displays.HelpEditor;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

import java.util.List;
import java.util.function.Consumer;

public class LanguageHelpTemplate extends AbstractLanguageHelpTemplate<EditorBox> {
	private Language language;
	private String release;
	private Consumer<CommandResult> createVersionListener;

	public LanguageHelpTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void release(String release) {
		this.release = release;
	}

	public void onCreateVersion(Consumer<CommandResult> listener) {
		this.createVersionListener = listener;
	}

	@Override
	public void didMount() {
		super.didMount();
		if (language == null) return;
		createHelpEditor();
		refresh();
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
		HelpEditor display = helpEditor.display();
		if (display == null) return;
		display.language(language);
		display.release(release);
		display.refresh();
		//helpField.value(box().languageManager().loadHelp(language, release));
	}

	private void createVersion() {
		notifyUser("Creating version...", UserMessage.Type.Loading);
		createVersion.readonly(true);
		createVersionListener.accept(box().commands(LanguageCommands.class).createRelease(language, release, username()));
		createVersion.readonly(false);
		hideUserNotification();
	}

	private HelpEditor createHelpEditor() {
		helpEditor.clear();
		helpEditor.display(new HelpEditor(box()));
		return helpEditor.display();
	}

}