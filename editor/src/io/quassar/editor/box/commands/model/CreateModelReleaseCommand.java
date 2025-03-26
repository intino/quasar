package io.quassar.editor.box.commands.model;

import io.intino.builderservice.schemas.Message;
import io.intino.ls.document.FileDocumentManager;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.builder.ModelBuilder;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.Command.ExecutionResult;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.OperationResult;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class CreateModelReleaseCommand extends Command<ExecutionResult> {
	public Model model;
	public String version;

	public CreateModelReleaseCommand(EditorBox box) {
		super(box);
	}

	@Override
	public ExecutionResult execute() {
		ExecutionResult result = compile();
		if (!result.success()) return result;
		return resultOf(box.modelManager().createRelease(model, version));
	}

	private ExecutionResult compile() {
		try {
			ModelBuilder builder = new ModelBuilder(model, version, box);
			return ExecutionResult.build(builder.build(author()));
		} catch (IOException e) {
			return ExecutionResult.build(List.of(new Message().kind(Message.Kind.ERROR).content(e.getMessage())));
		}
	}

	private URL url(String url) {
		try {
			return URI.create(url).toURL();
		} catch (MalformedURLException ignored) {
			return null;
		}
	}

	private ExecutionResult resultOf(OperationResult result) {
		return ExecutionResult.build(List.of(new Message().kind(result.success() ? Message.Kind.INFO : Message.Kind.ERROR).content(result.message())));
	}

}
