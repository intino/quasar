package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Model;

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
		return true;
	}

}
