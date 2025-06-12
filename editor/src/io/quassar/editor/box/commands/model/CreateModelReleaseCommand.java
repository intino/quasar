package io.quassar.editor.box.commands.model;

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
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.TarHelper;
import io.quassar.editor.model.*;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CreateModelReleaseCommand extends Command<CommandResult> {
	public Model model;
	public String version;

	public CreateModelReleaseCommand(EditorBox box) {
		super(box);
	}

	@Override
	public CommandResult execute() {
		CommandResult result = check();
		if (!result.success()) return result;
		if (box.modelManager().release(model, version).exists()) return resultOf(replaceRelease());
		else return resultOf(box.modelManager().createRelease(model, version));
	}

	private OperationResult replaceRelease() {
		OperationResult result = box.modelManager().replaceRelease(model, version);
		Language language = box.languageManager().getWithMetamodel(model);
		if (language == null) return result;
		LanguageRelease release = language.release(version);
		if (release == null) return result;
		BuildResult rebuildResult = rebuild(language, release);
		return !rebuildResult.success() ? OperationResult.Error(rebuildResult.messages().stream().map(Message::content).collect(Collectors.joining("; "))) : result;
	}

	private CommandResult check() {
		CheckModelCommand command = new CheckModelCommand(box);
		command.author = author;
		command.model = model;
		command.release = Model.DraftRelease;
		return command.execute();
	}

	private BuildResult rebuild(Language language, LanguageRelease release) {
		File destination = null;
		try {
			BuildResult result = new ModelBuilder(model, new GavCoordinates(language.group(), language.name(), release.version()), box).build(author);
			if (!result.success()) return result;
			Resource resource = result.zipArtifacts();
			destination = box.archetype().tmp().builds(UUID.randomUUID().toString());
			TarHelper.extract(resource.inputStream(), destination);
			box.languageManager().saveDsl(language, release.version(), LanguageHelper.dslOf(destination));
			box.languageManager().saveDslManifest(language, release.version(), LanguageHelper.dslManifestOf(destination));
			box.languageManager().saveGraph(language, release.version(), LanguageHelper.graphOf(destination));
			box.languageManager().saveParsers(language, release.version(), LanguageHelper.parsersOf(destination));
			return result;
		}
		catch (Exception | InternalServerError | NotFound e) {
			Logger.error(e);
			return BuildResult.failure(List.of(new Message().content(e.getMessage()).kind(Message.Kind.ERROR)));
		} finally {
			if (destination != null) destination.delete();
		}
	}

}
