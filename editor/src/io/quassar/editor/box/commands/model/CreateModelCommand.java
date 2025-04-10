package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

import java.io.InputStream;

public class CreateModelCommand extends Command<Model> {
	public String name;
	public String title;
	public String description;
	public GavCoordinates language;
	public boolean isTemplate = false;
	public String owner;

	public CreateModelCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Model execute() {
		Model model = box.modelManager().create(name, title, description, language, isTemplate, owner);
		Language language = box.languageManager().get(this.language);
		LanguageRelease release = language.release(this.language.version());
		Model template = release.template() != null ? box.modelManager().get(release.template()) : null;
		if (template != null) box.modelManager().copyWorkSpace(template, model);
		else createDefaultWorkspace(model);
		return model;
	}

	private void createDefaultWorkspace(Model model) {
		box.modelManager().createFile(model, ModelHelper.validWorkspaceFileName("Model.tara"), null, null);
		createDefaultReadme(model);
	}

	private void createDefaultReadme(Model model) {
		InputStream stream = CreateModelCommand.class.getResourceAsStream("/templates/readme.template.md");
		box.modelManager().createFile(model, io.quassar.editor.box.models.File.withResourcesPath(ModelHelper.validWorkspaceFileName("readme.md")), stream, null);
	}

}
