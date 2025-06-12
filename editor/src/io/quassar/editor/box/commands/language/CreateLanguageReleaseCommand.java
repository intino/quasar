package io.quassar.editor.box.commands.language;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.schemas.Message;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.builder.BuildResult;
import io.quassar.editor.box.builder.ModelBuilder;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.commands.model.CreateModelCommand;
import io.quassar.editor.box.util.*;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class CreateLanguageReleaseCommand extends Command<CommandResult> {
	public Language language;
	public String version;

	public CreateLanguageReleaseCommand(EditorBox box) {
		super(box);
	}

	@Override
	public CommandResult execute() {
		BuildResult result = build(box.modelManager().get(language.metamodel()));
		if (!result.success()) return resultOf(result);
		LanguageRelease previousRelease = language.lastRelease();
		LanguageRelease release = box.languageManager().createRelease(language, version);
		createHelp(language, release, previousRelease);
		copyExamples(language, release, previousRelease);
		Model template = createTemplate(language, release, previousRelease);
		release.template(template.id());
		return resultOf(result);
	}

	private BuildResult build(Model metamodel) {
		File destination = null;
		try {
			BuildResult result = new ModelBuilder(metamodel, new GavCoordinates(language.group(), language.name(), version), box).build(author);
			if (!result.success()) return result;
			Resource resource = result.zipArtifacts();
			destination = box.archetype().tmp().builds(UUID.randomUUID().toString());
			TarHelper.extract(resource.inputStream(), destination);
			box.languageManager().saveDsl(language, version, LanguageHelper.dslOf(destination));
			box.languageManager().saveDslManifest(language, version, LanguageHelper.dslManifestOf(destination));
			box.languageManager().saveGraph(language, version, LanguageHelper.graphOf(destination));
			box.languageManager().saveParsers(language, version, LanguageHelper.parsersOf(destination));
			return result;
		}
		catch (Exception | InternalServerError | NotFound e) {
			Logger.error(e);
			return BuildResult.failure(List.of(new Message().content(e.getMessage()).kind(Message.Kind.ERROR)));
		} finally {
			if (destination != null) destination.delete();
		}
	}

	private void createHelp(Language language, LanguageRelease release, LanguageRelease previousRelease) {
		if (previousRelease != null) box.languageManager().saveHelp(language, release.version(), box.languageManager().loadHelp(language, previousRelease.version()));
		else createDefaultHelp(language, release);
	}

	private void createDefaultHelp(Language language, LanguageRelease release) {
		try {
			InputStream stream = CreateModelCommand.class.getResourceAsStream("/templates/language.help.template.html");
			String content = stream != null ? IOUtils.toString(stream, StandardCharsets.UTF_8) : "";
			content = content.replace("$DSL$", language.name() + " " + release.version());
			box.languageManager().saveHelp(language, release.version(), content);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void copyExamples(Language language, LanguageRelease release, LanguageRelease previousRelease) {
		if (previousRelease == null) return;
		previousRelease.examples().forEach(e -> copyExample(language, release, e));
	}

	private void copyExample(Language language, LanguageRelease release, String example) {
		Model model = box.modelManager().get(example);
		Model copy = box.modelManager().clone(model, Model.DraftRelease, ShortIdGenerator.generate(), model.name(), author);
		copy.language(GavCoordinates.fromString(language, release));
		copy.title(model.title());
		copy.usage(Model.Usage.Example);
		release.addExample(copy.id());
	}

	private Model createTemplate(Language language, LanguageRelease release, LanguageRelease previousRelease) {
		return previousRelease != null ? copyTemplate(language, release, previousRelease) : createEmptyTemplate(language, release);
	}

	private Model copyTemplate(Language language, LanguageRelease release, LanguageRelease previousRelease) {
		Model model = box.modelManager().getTemplate(language, previousRelease);
		Model clone = box.modelManager().clone(model, Model.DraftRelease, ShortIdGenerator.generate(), ModelHelper.proposeName(), author);
		clone.language(GavCoordinates.fromString(language, release));
		clone.title(language.name());
		clone.usage(Model.Usage.Template);
		return clone;
	}

	private Model createEmptyTemplate(Language language, LanguageRelease release) {
		CreateModelCommand command = new CreateModelCommand(box);
		command.author = author;
		command.language = GavCoordinates.fromString(language, release);
		command.name = ModelHelper.proposeName();
		command.title = language.name();
		command.description = "";
		command.usage = Model.Usage.Template;
		command.owner = author;
		return command.execute();
	}

}
