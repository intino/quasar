package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

import java.io.InputStream;

public class CreateExampleModelCommand extends Command<Model> {
	public Language language;
	public LanguageRelease release;
	public String owner;

	public CreateExampleModelCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Model execute() {
		Model model = createModel();
		release.examples().add(model.id());
		box.languageManager().save(language);
		return model;
	}

	private Model createModel() {
		String name = ModelHelper.proposeName();
		CreateModelCommand command = new CreateModelCommand(box);
		command.author = author;
		command.language = GavCoordinates.from(language, release);
		command.name = name;
		command.title = name;
		command.description = "";
		command.owner = author;
		return command.execute();
	}

}

