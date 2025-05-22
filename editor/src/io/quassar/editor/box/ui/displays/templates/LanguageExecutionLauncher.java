package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.Formatters;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageExecution;
import io.quassar.editor.model.LanguageExecution.LocalLanguage;
import io.quassar.editor.model.Model;

import java.util.Arrays;
import java.util.List;

public class LanguageExecutionLauncher extends AbstractLanguageExecutionLauncher<EditorBox> {
	private Model model;
	private String release;
	private LocalLanguage selectedLocalLanguage;

	public LanguageExecutionLauncher(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String release) {
		this.release = release;
	}

	public void launch() {
		LanguageExecution execution = execution();
		switch (execution.type()) {
			case None -> notifyUser("No execution environment defined in language %s".formatted(model.language().artifactId()), UserMessage.Type.Info);
			case Local -> {
				if (localLanguageOptions().isEmpty()) notifyUser("No execution environment defined in language %s".formatted(model.language().artifactId()), UserMessage.Type.Info);
				else localDialog.open();
			}
			case Remote -> {
				if (execution.remoteConfiguration().isEmpty()) notifyUser("No remote environment defined in language %s".formatted(model.language().artifactId()), UserMessage.Type.Info);
				else launchForge();
			}
		}
	}

	@Override
	public void init() {
		super.init();
		localDialog.onOpen(e -> refreshLocalDialog());
		localLanguageSelector.onSelect(this::updateLocalLanguage);
		selectedLocalLanguage = LocalLanguage.Docker;
	}

	private void launchForge() {
		Language language = box().languageManager().get(model.language());
		if (language.name().equals(Language.Metta)) openSite(PathHelper.forgeUrl(model, release, session()));
		else openSite(execution().remoteConfiguration());
	}

	private void openSite(String site) {
		siteLauncher.site(withParams(site));
		siteLauncher.launch();
	}

	private void refreshLocalDialog() {
		List<String> options = localLanguageOptions();
		localLanguageSelector.clear();
		localLanguageSelector.addAll(options);
		localLanguageSelector.selection(selectedLocalLanguage.name());
		localLanguageSelector.visible(options.size() > 1);
		String content = withParams(execution().localConfiguration(selectedLocalLanguage));
		execution.value(content);
		copy.text(content);
	}

	private List<String> localLanguageOptions() {
		LanguageExecution execution = execution();
		return Arrays.stream(LocalLanguage.values()).filter(l -> !execution.localConfiguration(l).isEmpty()).map(Enum::name).toList();
	}

	private void updateLocalLanguage(SelectionEvent event) {
		List<String> selection = event.selection();
		selectedLocalLanguage = !selection.isEmpty() ? LocalLanguage.valueOf(selection.getFirst()) : LocalLanguage.Docker;
		refreshLocalDialog();
	}

	private LanguageExecution execution() {
		Language language = box().languageManager().get(model.language());
		return language.release(model.language().version()).execution();
	}

	private String withParams(String content) {
		return content.replace("[model]", model.id()).replace("[release]", release);
	}

}