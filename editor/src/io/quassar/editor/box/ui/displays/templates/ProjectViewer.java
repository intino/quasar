package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Model;

import java.util.List;
import java.util.function.Consumer;

public class ProjectViewer extends AbstractProjectViewer<EditorBox> {
	private Model model;
	private String project;
	private Consumer<Model> selectListener;

	public ProjectViewer(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void project(String project) {
		this.project = project;
	}

	public void onSelect(Consumer<Model> listener) {
		this.selectListener = listener;
	}

	@Override
	public void refresh() {
		super.refresh();
		List<String> moduleList = box().modelManager().modules(project, username());
		modules.clear();
		moduleList.forEach(p -> fill(p, modules.add()));
	}

	private void fill(String module, ProjectModuleViewer display) {
		display.model(model);
		display.onSelect(e -> selectListener.accept(e));
		display.module(module);
		display.refresh();
	}
}