package io.quassar.editor.box.commands.language;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.builder.ModelChecker;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.model.CreateModelCommand;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CreateLanguageReleaseCommand extends Command<LanguageRelease> {
	public Language language;
	public String version;

	public CreateLanguageReleaseCommand(EditorBox box) {
		super(box);
	}

	@Override
	public LanguageRelease execute() {
		Model metamodel = box.modelManager().get(language.metamodel());
		check(metamodel);
		File dsl = LanguageHelper.mavenDslFile(language, version, box);
		LanguageRelease release = box.languageManager().createRelease(language, version, dsl.exists() ? dsl : null);
		createDefaultHelp(language, release);
		Model template = createTemplateModel(language, release);
		release.template(template.id());
		return release;
	}

	private void check(Model metamodel) {
		try {
			new ModelChecker(metamodel, new GavCoordinates(metamodel.language().groupId(), language.name(), version), box).check(author);
		} catch (IOException e) {
			Logger.error(e);
		}
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

	private Model createTemplateModel(Language language, LanguageRelease release) {
		CreateModelCommand command = new CreateModelCommand(box);
		command.author = author;
		command.language = GavCoordinates.fromString(language, release);
		command.name = ModelHelper.proposeName();
		command.title = language.name();
		command.description = "";
		command.isTemplate = true;
		command.owner = author;
		return command.execute();
	}

}
