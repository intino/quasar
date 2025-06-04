package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.UserMessage;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.util.Formatters;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageExecution;
import io.quassar.editor.model.LanguageExecution.Type;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
		nameField.onChange(e -> saveName());
		executionSelector.onSelect(e -> saveType());
		localEnvironmentBlock.onInit(e -> initLocalEnvironmentBlock());
		localEnvironmentBlock.onShow(e -> refreshLocalEnvironmentBlock());
		remoteEnvironmentBlock.onInit(e -> initRemoteEnvironmentBlock());
		remoteEnvironmentBlock.onShow(e -> refreshRemoteEnvironmentBlock());
	}

	@Override
	public void refresh() {
		super.refresh();
		boolean hasCommits = hasCommits();
		selectVersionBlock.visible(release == null && language != null);
		versionBlock.visible(release != null && release() != null);
		versionNotCreatedBlock.visible(release != null && release() == null && hasCommits);
		refreshNoVersionsBlock(hasCommits);
		if (!versionBlock.isVisible()) return;
		LanguageExecution execution = execution();
		nameField.value(execution != null ? execution.name() : null);
		refreshDownloads();
		refreshMavenDependencies();
		refreshExecutionEnvironment();
	}

	private boolean hasCommits() {
		if (language == null) return false;
		Model metamodel = box().modelManager().get(language.metamodel());
		return metamodel != null && !metamodel.releases().isEmpty();
	}

	private void refreshNoVersionsBlock(boolean hasCommits) {
		noVersionsBlock.visible(language != null && !hasCommits);
		if (!noVersionsBlock.isVisible()) return;
		Model metamodel = box().modelManager().get(language.metamodel());
		metamodelLink.site(PathHelper.modelUrl(metamodel, session()));
	}

	private void refreshDownloads() {
		File graphFile = box().languageManager().loadGraph(language, release());
		//downloadsBlock.visible(graphFile != null);
		downloadsBlock.visible(false);
		if (!downloadsBlock.isVisible()) return;
		downloads.clear();
		fill(graphFile, downloads.add());
		//box().languageManager().loadReaders(language, release()).forEach(r -> fill(r, downloads.add()));
	}

	private void refreshMavenDependencies() {
		dependencies.clear();
		box().languageManager().loadParsers(language, release()).forEach(r -> fill(r, dependencies.add()));
	}

	private void refreshExecutionEnvironment() {
		LanguageExecution execution = execution();
		if (execution == null) execution = box().commands(LanguageCommands.class).createExecution(language, release, nameField.value(), Type.None, username());
		executionSelector.select(execution.type().name().toLowerCase() + "Option");
	}

	private void fill(File file, DownloadTemplate display) {
		display.language(language);
		display.release(release);
		display.file(file);
		display.refresh();
	}

	private void fill(File file, DependencyTemplate display) {
		display.language(language);
		display.release(release);
		display.file(file);
		display.refresh();
	}

	private void createVersion() {
		notifyUser("Creating version...", UserMessage.Type.Loading);
		createVersion.readonly(true);
		createVersionListener.accept(box().commands(LanguageCommands.class).createRelease(language, release, username()));
		createVersion.readonly(false);
		hideUserNotification();
	}

	private void saveName() {
		LanguageExecution execution = execution();
		box().commands(LanguageCommands.class).saveExecution(language, release, nameField.value(), execution != null ? execution.type() : Type.None, execution != null ? execution.content(execution.type()) : "", username());
	}

	private void saveType() {
		Type selected = selectedExecution();
		LanguageExecution execution = execution();
		box().commands(LanguageCommands.class).saveExecution(language, release, execution != null ? execution.name() : null, selected, execution != null ? execution.content(selected) : "", username());
	}

	private Type selectedExecution() {
		List<String> selection = executionSelector.selection();
		if (selection.isEmpty()) return Type.None;
		String selected = selection.getFirst();
		return Type.valueOf(Formatters.firstUpperCase(selected).replace("Option", ""));
	}

	private void initLocalEnvironmentBlock() {
		localField.onChange(e -> saveLocalConfiguration(e.value()));
		installationField.onChange(e -> saveInstallationUrl(e.value()));
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
		installationField.value(execution != null ? execution.installationUrl() : null);
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
		LanguageExecution execution = execution();
		String name = execution != null ? execution.name() : null;
		box().commands(LanguageCommands.class).saveExecution(language, release, name, Type.Remote, content, username());
		remoteField.error(errorMessage(content));
	}

	private void saveLocalConfiguration(String content) {
		LanguageExecution execution = execution();
		String name = execution != null ? execution.name() : null;
		box().commands(LanguageCommands.class).saveExecution(language, release, name, Type.Local, content, username());
		localField.error(errorMessage(content));
	}

	private void saveInstallationUrl(String url) {
		box().commands(LanguageCommands.class).saveExecutionProperties(language, release, url, username());
	}

	private LanguageExecution execution() {
		LanguageRelease languageRelease = language.release(release);
		return languageRelease != null ? languageRelease.execution() : null;
	}

	private String errorMessage(String content) {
		String data = content != null ? content.toLowerCase() : "";
		if (!data.contains("[commit]") && !data.contains("[commit-url]")) return translate("Invalid command. Make sure it includes [commit] or [commit-url] placeholder.");
		return null;
	}

	private LanguageRelease release() {
		return language.release(release);
	}

}