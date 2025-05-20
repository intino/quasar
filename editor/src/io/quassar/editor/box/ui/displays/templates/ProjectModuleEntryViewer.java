package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Model;
import org.apache.commons.collections4.sequence.DeleteCommand;

import java.util.function.Consumer;

public class ProjectModuleEntryViewer extends AbstractProjectModuleEntryViewer<EditorBox> {
	private Model model;
	private boolean selected;
	private Consumer<Boolean> selectListener;

	public ProjectModuleEntryViewer(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void selected(boolean value) {
		this.selected = value;
	}

	public void onSelect(Consumer<Boolean> listener) {
		this.selectListener = listener;
	}

	@Override
	public void init() {
		super.init();
		titleLink.onExecute(e -> open());
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshLink();
		refreshText();
	}

	private void refreshLink() {
		titleLink.visible(!selected);
		if (!titleLink.isVisible()) return;
		titleLink.title(model.language().languageId());
	}

	private void refreshText() {
		titleText.visible(selected);
		if (!titleText.isVisible()) return;
		titleText.value(model.language().languageId());
	}

	private void open() {
		notifier.dispatch(PathHelper.modelPath(model));
		selectListener.accept(true);
	}

}