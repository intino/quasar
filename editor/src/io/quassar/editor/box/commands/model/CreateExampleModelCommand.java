package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

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
		release.addExample(model.id());
		return model;
	}

	private Model createModel() {
		String name = ModelHelper.proposeName();
		CreateModelCommand command = new CreateModelCommand(box);
		command.author = author;
		command.language = GavCoordinates.fromString(language, release);
		command.name = name;
		command.title = name;
		command.description = "";
		command.usage = Model.Usage.Example;
		command.owner = author;
		return command.execute();
	}

}

