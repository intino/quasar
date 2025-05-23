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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;

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
		LanguageRelease release = box.languageManager().createRelease(language, version);
		createDefaultHelp(language, release);
		Model template = createTemplateModel(language, release);
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
			box.languageManager().saveDsl(language, version, dslOf(destination));
			box.languageManager().saveGraph(language, version, graphOf(destination));
			box.languageManager().saveReaders(language, version, readersOf(destination));
			return result;
		}
		catch (Exception | InternalServerError | NotFound e) {
			Logger.error(e);
			return BuildResult.failure(List.of(new Message().content(e.getMessage()).kind(Message.Kind.ERROR)));
		} finally {
			if (destination != null) destination.delete();
		}
	}

	private static final String LanguageDir = "language";
	private File dslOf(File destination) {
		File[] files = destination.listFiles();
		if (files == null) return null;
		File dir = Arrays.stream(files).filter(f -> f.getName().equals(LanguageDir)).findFirst().orElse(null);
		if (dir == null) return null;
		File[] languageFiles = dir.listFiles();
		if (languageFiles == null) return null;
		return Arrays.stream(languageFiles).filter(l -> l.getName().endsWith(".jar")).findFirst().orElse(null);
	}

	private static final String GraphFilename = "graph.json";
	private File graphOf(File destination) {
		File[] files = destination.listFiles();
		if (files == null) return null;
		return Arrays.stream(files).filter(f -> f.getName().equals(GraphFilename)).findFirst().orElse(null);
	}

	private List<File> readersOf(File destination) throws Exception {
		File[] files = destination.listFiles();
		if (files == null) return emptyList();
		return compressed(Arrays.stream(files).filter(f -> f.isDirectory() && !f.getName().equals(LanguageDir)).toList());
	}

	private static List<File> compressed(List<File> list) throws Exception {
		List<File> result = new ArrayList<>();
		for (File file : list) {
			File zipFile = new File(file.getParent(), file.getName().substring(file.getName().lastIndexOf("-")+1) + ".zip");
			ZipHelper.zip(file.toPath(), zipFile.toPath());
			result.add(zipFile);
		}
		return result;
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
