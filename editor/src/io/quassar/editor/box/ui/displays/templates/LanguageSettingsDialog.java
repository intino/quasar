package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class LanguageSettingsDialog extends AbstractLanguageSettingsDialog<EditorBox> {
	private Language language;
	private Consumer<Language> saveListener;
	private boolean saveReadme = false;
	private boolean saveAccess = false;

	public LanguageSettingsDialog(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void open() {
		dialog.open();
	}

	public void onSave(Consumer<Language> listener) {
		this.saveListener = listener;
	}

	@Override
	public void init() {
		super.init();
		dialog.onOpen(e -> refreshDialog());
		generalBlock.onInit(e -> initGeneralBlock());
		generalBlock.onShow(e -> refreshGeneralBlock());
		readmeBlock.onShow(e -> refreshReadmeBlock());
		accessBlock.onInit(e -> initAccessBlock());
		accessBlock.onShow(e -> refreshAccessBlock());
		saveSettings.onExecute(e -> saveSettings());
	}

	private void refreshDialog() {
		settingsTabSelector.select(0);
		saveReadme = false;
		saveAccess = false;
	}

	private void initGeneralBlock() {
		languageEditor.onRemove(e -> {
			dialog.close();
			notifier.dispatch(PathHelper.homePath());
		});
	}

	private void refreshGeneralBlock() {
		languageEditor.language(language);
		languageEditor.refresh();
	}

	private void refreshReadmeBlock() {
		try {
			saveReadme = true;
			File readme = box().archetype().languages().readme(language.name());
			readmeField.value(readme.exists() ? Files.readString(readme.toPath()) : null);
		} catch (IOException ignored) {
			readmeField.value(null);
		}
	}

	private void initAccessBlock() {
		accessTypeField.onToggle(e -> accessBlock.patternsBlock.visible(e.state() == ToggleEvent.State.On));
	}

	private void refreshAccessBlock() {
		saveAccess = true;
		accessTypeField.state(language.isPrivate() ? ToggleEvent.State.On : ToggleEvent.State.Off);
		accessBlock.patternsBlock.visible(language.isPrivate());
		patternField.value(String.join("; ", language.accessPatterns()));
	}

	private void saveSettings() {
		if (!check()) return;
		dialog.close();
		saveLanguage();
		saveReadme();
		saveAccess();
		saveModelProperties();
		saveListener.accept(language);
	}

	private void saveLanguage() {
		String hint = languageEditor.hint();
		String description = languageEditor.description();
		String fileExtension = languageEditor.fileExtension();
		List<String> tags = new ArrayList<>(languageEditor.tags());
		File logo = languageEditor.logo();
		box().commands(LanguageCommands.class).save(language, hint, description, fileExtension, Language.Level.L1, tags, logo, username());
	}

	private void saveReadme() {
		if (!saveReadme) return;
		box().commands(LanguageCommands.class).saveReadme(language, readmeField.value(), username());
	}

	private void saveAccess() {
		if (!saveAccess) return;
		boolean isPrivate = accessTypeField.state() == ToggleEvent.State.On;
		if (isPrivate) {
			List<String> accessPatterns = Arrays.stream(patternField.value().split(";")).map(String::trim).filter(s -> !s.isEmpty()).toList();
			box().commands(LanguageCommands.class).makePrivate(language, accessPatterns, username());
		}
		else box().commands(LanguageCommands.class).makePublic(language, username());
	}

	private void saveModelProperties() {
		Model model = box().modelManager().get(language.parent(), language.name());
		if (model == null) return;
		box().commands(ModelCommands.class).saveProperties(model, languageEditor.hint(), languageEditor.description(), username());
	}

	private boolean check() {
		return languageEditor.check();
	}

}