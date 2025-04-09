package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.box.util.SessionHelper;
import io.quassar.editor.model.FilePosition;
import io.quassar.editor.model.Model;

public class ModelTemplate extends AbstractModelTemplate<EditorBox> {
	private Model model;
	private String release;
	private ModelView selectedView;
	private io.quassar.editor.box.models.File selectedFile;
	private FilePosition selectedPosition;
	private boolean showHelp = false;

	public ModelTemplate(EditorBox box) {
		super(box);
	}

	public void openStarting(String model) {
		open(model, null, null, null, null, true);
	}

	public void openTemplate(String model) {
		open(model, Model.DraftRelease, null, null, null, false);
	}

	public void open(String model, String release) {
		open(model, release, null, null, null, false);
	}

	public void open(String model, String release, String view, String file, String position) {
		open(model, release, view, file, position, false);
	}

	public void open(String model, String release, String view, String file, String position, boolean showHelp) {
		this.model = box().modelManager().get(model);
		this.release = release != null ? release : Model.DraftRelease;
		this.selectedView = view != null ? ModelView.from(view) : SessionHelper.modelView(session());
		ModelContainer modelContainer = this.model != null ? box().modelManager().modelContainer(this.model, this.release) : null;
		this.selectedFile = file != null && modelContainer != null ? modelContainer.file(file) : null;
		this.selectedPosition = position != null ? FilePosition.from(position) : null;
		this.showHelp = showHelp;
		refresh();
	}

	@Override
	public void refresh() {
		super.refresh();
		notFoundBlock.visible(model == null);
		refreshContent();
	}

	private void refreshContent() {
		contentBlock.visible(model != null);
		if (!contentBlock.isVisible()) return;
		modelEditor.model(model, release);
		modelEditor.view(selectedView);
		modelEditor.file(selectedFile, selectedPosition);
		modelEditor.showHelp(showHelp);
		modelEditor.refresh();
	}

}