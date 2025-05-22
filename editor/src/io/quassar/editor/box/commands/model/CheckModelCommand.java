package io.quassar.editor.box.commands.model;

import io.intino.builderservice.schemas.Message;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.model.Model;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;

import java.util.List;

public class CheckModelCommand extends Command<CommandResult> {
	public Model model;
	public String release;

	public CheckModelCommand(EditorBox box) {
		super(box);
	}

	@Override
	public CommandResult execute() {
		CommandResult result = resultOf(box.modelManager().check(model, release));
		if (result.success() && !box.modelManager().hasWorkspaceMograms(model, release)) return noMogramsResult();
		return result;
	}

	private CommandResult resultOf(List<Diagnostic> diagnosticList) {
		List<Message> messages = diagnosticList.stream().map(this::messageOf).toList();
		return new CommandResult() {
			@Override
			public boolean success() {
				return messages.stream().noneMatch(m -> m.kind() == Message.Kind.ERROR);
			}

			@Override
			public List<Message> messages() {
				return messages;
			}
		};
	}

	private Message messageOf(Diagnostic diagnostic) {
		Position start = diagnostic.getRange().getStart();
		return new Message().uri(diagnostic.getSource()).kind(kindOf(diagnostic.getSeverity())).content(diagnostic.getMessage()).column(start.getCharacter()).line(start.getLine());
	}

	private Message.Kind kindOf(DiagnosticSeverity severity) {
		return switch (severity) {
			case Hint, Information -> Message.Kind.INFO;
			case Warning -> Message.Kind.WARNING;
			case Error -> Message.Kind.ERROR;
		};
	}

	private CommandResult noMogramsResult() {
		return new CommandResult() {
			@Override
			public boolean success() {
				return false;
			}

			@Override
			public List<Message> messages() {
				return List.of(new Message().kind(Message.Kind.ERROR).content("Could not check. Model is empty"));
			}
		};
	}

}
