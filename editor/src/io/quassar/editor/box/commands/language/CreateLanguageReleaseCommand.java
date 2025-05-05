package io.quassar.editor.box.commands.language;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.builder.BuildResult;
import io.quassar.editor.box.builder.ModelBuilder;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.model.CreateModelCommand;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.TarHelper;
import io.quassar.editor.box.util.ZipHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;

public class CreateLanguageReleaseCommand extends Command<LanguageRelease> {
	public Language language;
	public String version;

	public CreateLanguageReleaseCommand(EditorBox box) {
		super(box);
	}

	@Override
	public LanguageRelease execute() {
		build(box.modelManager().get(language.metamodel()));
		File dsl = LanguageHelper.mavenDslFile(language, version, box);
		LanguageRelease release = box.languageManager().createRelease(language, version, dsl.exists() ? dsl : null);
		createDefaultHelp(language, release);
		Model template = createTemplateModel(language, release);
		release.template(template.id());
		return release;
	}

	private void build(Model metamodel) {
		File destination = null;
		try {
			BuildResult result = new ModelBuilder(metamodel, new GavCoordinates(metamodel.language().groupId(), language.name(), version), box).build(author);
			Resource resource = result.zipArtifacts();
			destination = box.archetype().tmp().builds(UUID.randomUUID().toString());
			TarHelper.extract(resource.inputStream(), destination);
			box.languageManager().saveGraph(language, version, graphOf(destination));
			box.languageManager().saveReaders(language, version, readersOf(destination));
		}
		catch (IOException | InternalServerError | NotFound e) {
			Logger.error(e);
		}
		finally {
			if (destination != null) destination.delete();
		}
	}

	private static final String GraphFilename = "graph.json";
	private File graphOf(File destiny) {
		File[] files = destiny.listFiles();
		if (files == null) return null;
		return Arrays.stream(files).filter(f -> f.getName().equals(GraphFilename)).findFirst().orElse(null);
	}

	private static final String ReaderPrefix = "reader-";
	private List<File> readersOf(File destiny) {
		File[] files = destiny.listFiles();
		if (files == null) return emptyList();
		return Arrays.stream(files).filter(f -> f.getName().startsWith(ReaderPrefix)).toList();
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
		command.usage = Model.Usage.Template;
		command.owner = author;
		return command.execute();
	}

}
