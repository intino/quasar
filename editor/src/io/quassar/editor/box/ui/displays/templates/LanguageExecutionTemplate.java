package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.UserMessage;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.util.Formatters;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageExecution;
import io.quassar.editor.model.LanguageExecution.Type;
import io.quassar.editor.model.LanguageRelease;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;

public class LanguageExecutionTemplate extends AbstractLanguageExecutionTemplate<EditorBox> {
	private Language language;
	private String release;
	private Consumer<CommandResult> createVersionListener;

	public LanguageExecutionTemplate(EditorBox box) {
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
	public void init() {
		super.init();
		createVersion.onExecute(e -> createVersion());
		executionSelector.onSelect(e -> saveType());
		localEnvironmentBlock.onInit(e -> initLocalEnvironmentBlock());
		localEnvironmentBlock.onShow(e -> refreshLocalEnvironmentBlock());
		remoteEnvironmentBlock.onInit(e -> initRemoteEnvironmentBlock());
		remoteEnvironmentBlock.onShow(e -> refreshRemoteEnvironmentBlock());
	}

	@Override
	public void refresh() {
		super.refresh();
		LanguageRelease languageRelease = language.release(release);
		selectVersionBlock.visible(release == null);
		versionBlock.visible(release != null && languageRelease != null);
		versionNotCreatedBlock.visible(release != null && languageRelease == null);
		if (languageRelease == null) return;
		LanguageExecution execution = execution();
		if (execution == null) execution = box().commands(LanguageCommands.class).createExecution(language, release, Type.None, username());
		executionSelector.select(execution.type().name().toLowerCase() + "Option");
	}

	private void createVersion() {
		notifyUser("Creating version...", UserMessage.Type.Loading);
		createVersion.readonly(true);
		createVersionListener.accept(box().commands(LanguageCommands.class).createRelease(language, release, username()));
		createVersion.readonly(false);
		hideUserNotification();
	}

	private void saveType() {
		Type selected = selectedExecution();
		LanguageExecution execution = execution();
		box().commands(LanguageCommands.class).saveExecution(language, release, selected, execution != null ? execution.content(selected) : "", username());
	}

	private Type selectedExecution() {
		List<String> selection = executionSelector.selection();
		if (selection.isEmpty()) return Type.None;
		String selected = selection.getFirst();
		return Type.valueOf(Formatters.firstUpperCase(selected).replace("Option", ""));
	}

	private void initLocalEnvironmentBlock() {
		localField.onChange(e -> saveLocalConfiguration(e.value()));
		insertTemplate.onExecute(e -> insertTemplate());
	}

	private void insertTemplate() {
		String selectedLanguage = !templateSelector.selection().isEmpty() ? templateSelector.selection().getFirst() : null;
		if (selectedLanguage == null) return;
		localField.value(templateContent(selectedLanguage));
		saveLocalConfiguration(localField.value());
		localField.focus();
	}

	private String templateContent(String language) {
		try {
			InputStream stream = LanguageExecutionTemplate.class.getResourceAsStream("/templates/execution/%s.tpl".formatted(language));
			if (stream == null) return "";
			return IOUtils.toString(stream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			Logger.error(e);
			return "";
		}
	}

	private static final List<String> Languages = List.of("Docker", "Maven", "Python", "Custom");
	private void refreshLocalEnvironmentBlock() {
		LanguageExecution execution = execution();
		localField.value(execution != null ? execution.content(Type.Local) : null);
		templateSelector.clear();
		templateSelector.addAll(Languages);
		templateSelector.selection(Languages.getFirst());
	}

	private void initRemoteEnvironmentBlock() {
		remoteField.onChange(e -> saveRemoteConfiguration(e.value()));
	}

	private void refreshRemoteEnvironmentBlock() {
		LanguageExecution execution = execution();
		remoteField.value(execution != null ? execution.content(Type.Remote) : null);
	}

	private void saveRemoteConfiguration(String content) {
		box().commands(LanguageCommands.class).saveExecution(language, release, Type.Remote, content, username());
		remoteField.error(errorMessage(content));
	}

	private void saveLocalConfiguration(String content) {
		box().commands(LanguageCommands.class).saveExecution(language, release, Type.Local, content, username());
		localField.error(errorMessage(content));
	}

	private LanguageExecution execution() {
		LanguageRelease languageRelease = language.release(release);
		return languageRelease != null ? languageRelease.execution() : null;
	}

	private String errorMessage(String content) {
		String data = content != null ? content.toLowerCase() : "";
		if (!data.contains("[model]")) return translate("Invalid command. Make sure it includes both [model] and [release] placeholders.");
		if (!data.contains("[release]")) return translate("Invalid command. Make sure it includes both [model] and [release] placeholders.");
		return null;
	}

}