package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Model;

import java.util.List;
import java.util.function.Consumer;

public class ProjectModuleViewer extends AbstractProjectModuleViewer<EditorBox> {
	private Model model;
	private String module;
	private boolean showTitle = true;
	private Consumer<Model> selectListener;

	public ProjectModuleViewer(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void module(String module) {
		this.module = module;
	}

	public void showTitle(boolean value) {
		this.showTitle = value;
	}

	public void onSelect(Consumer<Model> listener) {
		this.selectListener = listener;
	}

	@Override
	public void refresh() {
		super.refresh();
		moduleName.visible(showTitle);
		if (showTitle) moduleName.value(this.module);
		List<Model> models = box().modelManager().models(model.project(), module, username());
		entries.clear();
		models.forEach(m -> fill(m, entries.add()));
	}

	private void fill(Model model, ProjectModuleEntryViewer display) {
		display.model(model);
		display.selected(model.id().equals(this.model.id()));
		display.onSelect(e -> selectListener.accept(model));
		display.refresh();
	}

}