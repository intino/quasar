package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageExecution;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.ModelRelease;

public class LanguageExecutionLauncher extends AbstractLanguageExecutionLauncher<EditorBox> {
	private Model model;
	private String release;

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
		if (execution == null) {
			notifyUser("No execution environment defined in language %s".formatted(model.language().artifactId()), UserMessage.Type.Info);
			return;
		}
		switch (execution.type()) {
			case None -> notifyUser("No execution environment defined in language %s".formatted(model.language().artifactId()), UserMessage.Type.Info);
			case Local -> {
				if (execution.content().isEmpty()) notifyUser("No execution environment defined in language %s".formatted(model.language().artifactId()), UserMessage.Type.Info);
				else localDialog.open();
			}
			case Remote -> {
				if (execution.content().isEmpty()) notifyUser("No remote environment defined in language %s".formatted(model.language().artifactId()), UserMessage.Type.Info);
				else launchForge();
			}
		}
	}

	@Override
	public void init() {
		super.init();
		localDialog.onOpen(e -> refreshLocalDialog());
	}

	private void launchForge() {
		Language language = box().languageManager().get(model.language());
		if (language.name().equals(Language.Metta)) openSite(PathHelper.forgeUrl(model, release, session()));
		else openSite(execution().content());
	}

	private void openSite(String site) {
		notifier.redirect(withParams(site));
//		siteLauncher.site(withParams(site));
//		siteLauncher.launch();
	}

	private void refreshLocalDialog() {
		String content = withParams(execution().content());
		execution.value(content);
		copy.text(content);
		openInstallationNotes.visible(!execution().installationUrl().isEmpty());
		openInstallationNotes.site(execution().installationUrl());
	}

	private LanguageExecution execution() {
		Language language = box().languageManager().get(model.language());
		return language.release(model.language().version()).execution();
	}

	private String withParams(String content) {
		ModelRelease modelRelease = model.release(release);
		return content.replace("[commit]", modelRelease.commit()).replace("[commit-url]", PathHelper.commitUrl(modelRelease, session()));
	}

}