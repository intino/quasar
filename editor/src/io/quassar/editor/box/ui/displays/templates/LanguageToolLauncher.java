package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageTool;
import io.quassar.editor.model.Model;

import java.util.Collections;
import java.util.List;

public class LanguageToolLauncher extends AbstractLanguageToolLauncher<EditorBox> {
	private Model model;
	private String release;
	private LanguageTool selectedTool;

	public LanguageToolLauncher(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String release) {
		this.release = release;
	}

	@Override
	public void init() {
		super.init();
		dialog.onOpen(e -> refreshDialog());
		toolSelector.onSelect(this::launch);
		dockerDialog.onOpen(e -> refreshDockerDialog());
		manualDialog.onOpen(e -> refreshManualDialog());
	}

	public void launch() {
		List<LanguageTool> tools = tools();
		if (tools.isEmpty()) {
			notifyUser("No tools defined in language %s".formatted(model.language().artifactId()), UserMessage.Type.Info);
			return;
		}
		if (tools.size() == 1) {
			launch(tools.getFirst());
			return;
		}
		dialog.open();
	}

	private void launch(SelectionEvent event) {
		List<String> selection = event.selection();
		if (selection.isEmpty()) return;
		dialog.close();
		launch(selectedTool());
	}

	private void launch(LanguageTool tool) {
		if (tool == null) return;
		this.selectedTool = tool;
		if (tool.type() == LanguageTool.Type.Docker) dockerDialog.open();
		else if (tool.type() == LanguageTool.Type.Site) openForgeSite();
		else if (tool.type() == LanguageTool.Type.Manual) manualDialog.open();
	}

	private void openForgeSite() {
		openForgeSite.site(paramValueOf("url"));
		openForgeSite.launch();
	}

	private LanguageTool selectedTool() {
		if (toolSelector.selection().isEmpty()) return null;
		String selected = toolSelector.selection().getFirst();
		return tools().stream().filter(t -> t.name().equals(selected)).findFirst().orElse(null);
	}

	private void refreshDialog() {
		toolSelector.clear();
		toolSelector.addAll(tools().stream().map(LanguageTool::name).toList());
		toolSelector.selection(Collections.emptyList());
	}

	private void refreshDockerDialog() {
		String url = paramValueOf("url");
		String params = paramValueOf("params");
		pull.value("1. docker pull %s".formatted(url));
		copyPull.text("docker pull %s".formatted(url));
		run.value("2. docker run %s%s".formatted(!params.isEmpty() ? params + " " : "", url));
		copyRun.text("docker run %s%s".formatted(!params.isEmpty() ? params + " " : "", url));
	}

	private void refreshManualDialog() {
		String installInfo = paramValueOf("install");
		String executionInfo = paramValueOf("execution");
		install.value(installInfo);
		execution.value(executionInfo);
	}

	private String paramValueOf(String name) {
		LanguageTool.Parameter parameter = selectedTool.parameter(name);
		return parameter != null ? parameter.value().replace("[model]", model.id()).replace("[release]", release) : "";
	}

	private List<LanguageTool> tools() {
		Language language = box().languageManager().get(model.language());
		return language.release(model.language().version()).tools();
	}

}