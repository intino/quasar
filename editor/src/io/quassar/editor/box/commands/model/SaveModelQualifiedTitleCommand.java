package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.ui.displays.templates.LanguageKitTemplate;
import io.quassar.editor.model.Model;

import java.util.Collection;
import java.util.Objects;

public class SaveModelQualifiedTitleCommand extends Command<Boolean> {
	public Model model;
	public String project;
	public String module;

	public SaveModelQualifiedTitleCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		model.qualifiedTitle(project.toUpperCase(), module.toUpperCase());
		notifyTitleChange();
		return true;
	}

	private void notifyTitleChange() {
		if (!model.isExample()) return;
		box.souls().stream().filter(Objects::nonNull).map(s -> s.displays(LanguageKitTemplate.class)).flatMap(Collection::stream).distinct().forEach(d -> d.notifyChange(model));
	}

}
