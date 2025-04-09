package io.quassar.editor.box.commands.language;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.model.CreateModelCommand;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class CreateLanguageCommand extends Command<Language> {
	public String name;
	public String version;
	public GavCoordinates parent;
	public Language.Level level;
	public String hint;
	public String description;

	public CreateLanguageCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		Language language = box.languageManager().create(name, level, hint, description, parent);
		File dsl = LanguageHelper.mavenDslFile(language, version, box);
		LanguageRelease release = box.languageManager().createRelease(language, version, dsl.exists() ? dsl : null);
		createDefaultHelp(language, release);
		createTemplateModel(language, release);
		return language;
	}

	private void createDefaultHelp(Language language, LanguageRelease release) {
		try {
			InputStream stream = CreateModelCommand.class.getResourceAsStream("/templates/language.template.html");
			String content = stream != null ? IOUtils.toString(stream, StandardCharsets.UTF_8) : "";
			box.languageManager().saveHelp(language, release.version(), content);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void createTemplateModel(Language language, LanguageRelease release) {
		// TODO CREAR EL TEMPLATE
/*		CreateModelCommand command = new CreateModelCommand(box);
		command.author = author;
		command.language = language;
		command.name = Model.Template;
		command.title = language.name();
		command.hint = "";
		command.description = "";
		command.owner = author;
		command.execute();*/
	}

}
