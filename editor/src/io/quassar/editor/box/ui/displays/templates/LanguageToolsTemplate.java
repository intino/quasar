package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.LanguageTool;

import javax.tools.Tool;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class LanguageToolsTemplate extends AbstractLanguageToolsTemplate<EditorBox> {
	private Language language;
	private String release;
	private Consumer<CommandResult> createVersionListener;

	public LanguageToolsTemplate(EditorBox box) {
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
		toolNameField.onEnterPress(e -> wizard.next());
		dialog.onOpen(e -> refreshDialog());
		step1.canNext(e -> checkName());
		step2.onShow(e -> refreshStep2());
		step3.onShow(e -> refreshStep3());
		wizard.onFinish(e -> addTool());
	}

	@Override
	public void refresh() {
		super.refresh();
		LanguageRelease languageRelease = language.release(release);
		selectVersionBlock.visible(release == null);
		versionBlock.visible(release != null && languageRelease != null);
		versionNotCreatedBlock.visible(release != null && languageRelease == null);
		if (languageRelease == null) return;
		tools.clear();
		languageRelease.tools().forEach(t -> fill(t, tools.add()));
	}

	private void fill(LanguageTool tool, LanguageToolTemplate display) {
		display.tool(tool);
		display.onRemove(e -> removeTool(tool));
		display.refresh();
	}

	private void createVersion() {
		notifyUser("Creating version...", UserMessage.Type.Loading);
		createVersion.readonly(true);
		createVersionListener.accept(box().commands(LanguageCommands.class).createRelease(language, release, username()));
		createVersion.readonly(false);
		hideUserNotification();
	}

	private void refreshDialog() {
		wizard.reset();
		toolEditor.reset();
	}

	private boolean checkName() {
		return DisplayHelper.check(toolNameField, this::translate);
	}

	private void refreshStep2() {
		if (!toolTypeSelector.selection().isEmpty()) return;
		toolTypeSelector.select("dockerImageFactoryOption");
	}

	private void refreshStep3() {
		toolEditor.type(toolType());
		toolEditor.refresh();
	}

	private void addTool() {
		if (!check()) return;
		dialog.close();
		box().commands(LanguageCommands.class).addTool(language, release, toolNameField.value(), toolType(), parameters(), username());
		refresh();
	}

	private void removeTool(LanguageTool tool) {
		box().commands(LanguageCommands.class).removeTool(language, release, tool, username());
		refresh();
	}

	private LanguageTool.Type toolType() {
		String selected = toolTypeSelector.selection().getFirst();
		if (selected.equals("dockerImageFactoryOption")) return LanguageTool.Type.Docker;
		else if (selected.equals("deploySiteFactoryOption")) return LanguageTool.Type.Site;
		else return LanguageTool.Type.Manual;
	}

	private boolean check() {
		if (!DisplayHelper.check(toolNameField, this::translate)) return false;
		return toolEditor.check();
	}

	private Map<String, String> parameters() {
		return toolEditor.parameters();
	}

}