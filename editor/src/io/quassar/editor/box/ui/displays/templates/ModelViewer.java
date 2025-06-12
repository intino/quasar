package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.box.ui.displays.IntinoDslEditor;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.Model;

import java.util.Collections;
import java.util.List;

public class ModelViewer extends AbstractModelViewer<EditorBox> {
	private Model model;
	private String release;
	private ModelContainer modelContainer;
	private File selectedFile;

	public ModelViewer(EditorBox box) {
		super(box);
	}

	public void model(Model model, String release) {
		this.model = model;
		this.release = release;
		this.modelContainer = this.model != null ? box().modelManager().modelContainer(this.model, this.release) : null;
		this.selectedFile = null;
	}

	public void file(File file) {
		this.selectedFile = file;
	}

	@Override
	public void didMount() {
		super.didMount();
		if (model == null) return;
		createFileEditor();
		refresh();
	}

	@Override
	public void init() {
		super.init();
		fileSelector.onSelect(this::openModelFile);
		initFileEditor();
	}

	public void reset() {
		createFileEditor();
		refresh();
	}

	@Override
	public void refresh() {
		super.refresh();
		languageNotLoadedBlock.visible(modelContainer == null);
		refreshContent();
	}

	private void refreshContent() {
		contentBlock.visible(model != null && modelContainer != null);
		if (!contentBlock.isVisible()) return;
		List<File> files = modelContainer.modelFiles();
		noFilesMessage.visible(files.isEmpty());
		refreshFileSelector(files);
		refreshFile(files);
	}

	private void initFileEditor() {
		createFileEditor();
	}

	private void refreshFileSelector(List<File> files) {
		fileSelector.visible(!files.isEmpty());
		if (!fileSelector.isVisible()) return;
		fileSelector.clear();
		fileSelector.addAll(files.stream().map(File::uri).toList());
		if (selectedFile() != null) fileSelector.selection(selectedFile().uri());
	}

	private IntinoDslEditor createFileEditor() {
		IntinoDslEditor editor = new IntinoDslEditor(box());
		intinoDslEditor.clear();
		intinoDslEditor.display(editor);
		editor.files(editorFiles());
		editor.onFileModified(e -> {});
		editor.onSelectFile(e -> selectedFile());
		editor.onSaveFile(e -> {});
		editor.onBuild(e -> {});
		return editor;
	}

	private List<File> editorFiles() {
		if (modelContainer == null) return Collections.emptyList();
		return modelContainer.modelFiles();
	}

	private void refreshFile(List<File> files) {
		IntinoDslEditor display = intinoDslEditor.display();
		if (display == null) display = createFileEditor();
		display.model(model);
		display.release(release);
		display.view(ModelView.Model);
		if (!display.initialized()) {
			display.files(editorFiles());
			display.file(selectedFile(), null);
			display.refresh();
		}
		else display.openFile(selectedFile(), null);
	}

	private File selectedFile() {
		if (selectedFile != null) return selectedFile;
		return ModelHelper.mainFile(modelContainer);
	}

	private void openModelFile(SelectionEvent e) {
		List<String> selection = e.selection();
		if (selection.isEmpty()) return;
		String uri = selection.getFirst();
		ModelContainer modelContainer = box().modelManager().modelContainer(model, release);
		File selected = modelContainer.modelFiles().stream().filter(f -> f.uri().equalsIgnoreCase(uri)).findFirst().orElse(null);
		file(selected);
		refresh();
	}

}