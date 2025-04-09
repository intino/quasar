package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class LanguageSettingsDialog extends AbstractLanguageSettingsDialog<EditorBox> {
	private Language language;
	private Consumer<Language> saveListener;
	private boolean saveHelp = false;
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
		helpBlock.onShow(e -> refreshHelpBlock());
		accessBlock.onShow(e -> refreshAccessBlock());
		saveSettings.onExecute(e -> saveSettings());
	}

	private void refreshDialog() {
		settingsTabSelector.select(0);
		saveHelp = false;
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

	private void refreshHelpBlock() {
		saveHelp = true;
		// TODO MC
		// helpField.value(box().languageManager().loadHelp(release));
	}

	private void refreshAccessBlock() {
		saveAccess = true;
		patternField.value(String.join("; ", language.access()));
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
		String title = languageEditor.title();
		String description = languageEditor.description();
		List<String> tags = new ArrayList<>(languageEditor.tags());
		File logo = languageEditor.logo();
		box().commands(LanguageCommands.class).save(language, title, description, Language.Level.L1, tags, logo, username());
	}

	private void saveReadme() {
		if (!saveHelp) return;
		// TODO MC
		// box().commands(LanguageCommands.class).saveHelp(release, helpField.value(), username());
	}

	private void saveAccess() {
		if (!saveAccess) return;
		List<String> accessPatterns = Arrays.stream(patternField.value().split(";")).map(String::trim).filter(s -> !s.isEmpty()).toList();
		box().commands(LanguageCommands.class).saveAccess(language, accessPatterns, username());
	}

	private void saveModelProperties() {
		Model model = box().modelManager().get(language.metamodel());
		if (model == null) return;
		box().commands(ModelCommands.class).saveProperties(model, languageEditor.title(), languageEditor.description(), username());
	}

	private boolean check() {
		return languageEditor.check();
	}

}