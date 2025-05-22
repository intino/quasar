package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.util.Formatters;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageExecution;
import io.quassar.editor.model.LanguageExecution.LocalLanguage;
import io.quassar.editor.model.LanguageRelease;

import java.util.List;
import java.util.function.Consumer;

public class LanguageExecutionTemplate extends AbstractLanguageExecutionTemplate<EditorBox> {
	private Language language;
	private String release;
	private Consumer<CommandResult> createVersionListener;
	private LocalLanguage selectedLocalLanguage;

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
		if (execution == null) execution = box().commands(LanguageCommands.class).createExecution(language, release, LanguageExecution.Type.None, username());
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
		LanguageExecution.Type selected = selectedExecution();
		box().commands(LanguageCommands.class).saveExecution(language, release, selected, username());
	}

	private LanguageExecution.Type selectedExecution() {
		List<String> selection = executionSelector.selection();
		if (selection.isEmpty()) return LanguageExecution.Type.None;
		String selected = selection.getFirst();
		return LanguageExecution.Type.valueOf(Formatters.firstUpperCase(selected).replace("Option", ""));
	}

	private void initLocalEnvironmentBlock() {
		localField.onChange(e -> saveLocalConfiguration(e.value()));
		localLanguageSelector.onSelect(this::updateLocalLanguage);
		selectedLocalLanguage = LocalLanguage.Docker;
	}

	private void refreshLocalEnvironmentBlock() {
		LanguageExecution execution = execution();
		localLanguageSelector.selection(Formatters.firstLowerCase(selectedLocalLanguage.name()) + "Option");
		localField.value(execution != null ? execution.localConfiguration(selectedLocalLanguage) : null);
	}

	private void updateLocalLanguage(SelectionEvent event) {
		List<String> selection = event.selection();
		this.selectedLocalLanguage = !selection.isEmpty() ? LocalLanguage.valueOf(Formatters.firstUpperCase(selection.getFirst()).replace("Option", "")) : LocalLanguage.Docker;
		localField.focus();
		refreshLocalEnvironmentBlock();
	}

	private void initRemoteEnvironmentBlock() {
		remoteField.onChange(e -> saveRemoteConfiguration(e.value()));
	}

	private void refreshRemoteEnvironmentBlock() {
		LanguageExecution execution = execution();
		remoteField.value(execution != null ? execution.remoteConfiguration() : null);
	}

	private void saveRemoteConfiguration(String content) {
		box().commands(LanguageCommands.class).saveRemoteExecution(language, release, content, username());
		remoteField.error(errorMessage(content));
	}

	private void saveLocalConfiguration(String content) {
		box().commands(LanguageCommands.class).saveLocalExecution(language, release, selectedLocalLanguage, content, username());
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