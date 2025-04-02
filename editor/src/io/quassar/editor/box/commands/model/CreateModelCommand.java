package io.quassar.editor.box.commands.model;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class CreateModelCommand extends Command<Model> {
	public String name;
	public String title;
	public String description;
	public Language language;
	public String owner;

	public CreateModelCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Model execute() {
		Model model = box.modelManager().create(name, title, description, language, owner);
		box.modelManager().createFile(model, ModelHelper.validWorkspaceFileName("Model.tara"), null, null);
		createDefaultReadme(model);
		return model;
	}

	private void createDefaultReadme(Model model) {
		try {
			InputStream stream = CreateModelCommand.class.getResourceAsStream("/readme.template.md");
			String content = stream != null ? IOUtils.toString(stream, StandardCharsets.UTF_8) : "";
			box.modelManager().createFile(model, Model.withResourcesPath(ModelHelper.validWorkspaceFileName("readme.md")), content, null);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

}
