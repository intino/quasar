package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.InputStream;

public class CreateModelCommand extends Command<Model> {
	public String name;
	public String title;
	public String hint;
	public String description;
	public Language language;
	public String owner;

	public CreateModelCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Model execute() {
		Model model = box.modelManager().create(name, title, hint, description, language, owner);
		box.modelManager().createFile(model, ModelHelper.validWorkspaceFileName("Model.tara"), null, null);
		createDefaultReadme(model);
		return model;
	}

	private void createDefaultReadme(Model model) {
		InputStream stream = CreateModelCommand.class.getResourceAsStream("/templates/readme.template.md");
		box.modelManager().createFile(model, io.quassar.editor.box.models.File.withResourcesPath(ModelHelper.validWorkspaceFileName("readme.md")), stream, null);
	}

}
