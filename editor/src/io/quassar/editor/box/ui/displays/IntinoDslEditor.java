package io.quassar.editor.box.ui.displays;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.schemas.IntinoDslEditorFile;
import io.quassar.editor.box.schemas.IntinoDslEditorFileContent;
import io.quassar.editor.box.schemas.IntinoDslEditorFilePosition;
import io.quassar.editor.box.schemas.IntinoDslEditorSetup;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.FilePosition;
import io.quassar.editor.model.Model;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class IntinoDslEditor extends AbstractIntinoDslEditor<EditorBox> {
	private Model model;
	private String release;
	private ModelView view;
	private List<File> files = new ArrayList<>();
	private File selectedFile;
	private FilePosition selectedPosition;
	private Consumer<Boolean> fileModifiedListener;
	private Consumer<IntinoDslEditorFileContent> saveFileListener;
	private Consumer<Boolean> buildListener;
	private Function<String, File> selectedFileListener;
	private boolean reloadRequired;

	public IntinoDslEditor(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String value) {
		this.release = value;
	}

	public void view(ModelView value) {
		this.view = value;
	}

	public boolean initialized() {
		if (selectedFile == null) return false;
		return model != null && release != null && files.stream().anyMatch(f -> f.uri().equals(selectedFile.uri()));
	}

	public boolean sameReleaseAndFile(String newRelease, String newUri) {
		return release != null && release.equals(newRelease) && selectedFile != null && selectedFile.uri().equals(newUri);
	}

	public void files(List<File> files) {
		this.files = files;
	}

	public void file(File file, FilePosition position) {
		this.reloadRequired = this.selectedFile == null || !this.selectedFile.uri().equals(file.uri());
		this.selectedFile = file;
		this.selectedPosition = position;
	}

	public void onFileModified(Consumer<Boolean> listener) {
		this.fileModifiedListener = listener;
	}

	public void onSaveFile(Consumer<IntinoDslEditorFileContent> listener) {
		this.saveFileListener = listener;
	}

	public void onBuild(Consumer<Boolean> listener) {
		this.buildListener = listener;
	}

	public void onSelectFile(Function<String, File> listener) {
		this.selectedFileListener = listener;
	}

	public void fireSavingProcess() {
		notifier.receiveContent();
	}

	public void fileModified() {
		if (fileModifiedListener == null) return;
		fileModifiedListener.accept(true);
	}

	public void fileContent(IntinoDslEditorFileContent content) {
		if (saveFileListener == null) return;
		saveFileListener.accept(content);
	}

	public void executeCommand(String name) {
		if (name.equalsIgnoreCase("build")) buildListener.accept(true);
	}

	public void fileSelected(String uri) {
		selectedFile = selectedFileListener.apply(uri);
	}

	public void openFile(File file, FilePosition position) {
		selectedFile = file;
		selectedPosition = position;
		notifier.refreshReadonly(!PermissionsHelper.canEdit(model, release, session(), box()));
		notifier.refreshFile(fileOf(file).position(positionOf(position)));
	}

	@Override
	public void refresh() {
		super.refresh();
		if (model == null) return;
		reload();
	}

	private void reload() {
		notifier.setup(info());
	}

	private IntinoDslEditorSetup info() {
		IntinoDslEditorSetup result = new IntinoDslEditorSetup();
		result.dslName(model.language().artifactId());
		result.modelName(model.name());
		result.modelRelease(release);
		result.readonly(!PermissionsHelper.canEdit(model, release, session(), box()));
		result.fileAddress(PathHelper.modelPath(model, release, view, ":file"));
		result.files(files.stream().map(this::fileOf).toList());
		return result;
	}

	private IntinoDslEditorFile fileOf(File file) {
		boolean active = this.selectedFile != null && this.selectedFile.uri().equals(file.uri());
		return new IntinoDslEditorFile().active(active).name(file.name()).uri(file.uri()).extension(file.extension()).content(content(file)).language(file.language()).position(positionOf(selectedPosition));
	}

	private IntinoDslEditorFilePosition positionOf(FilePosition position) {
		if (position == null) return null;
		return new IntinoDslEditorFilePosition().line(position.line()).column(position.column());
	}

	private String content(File file) {
		try {
			InputStream content = box().modelManager().content(model, release, file.uri());
			return content != null ? IOUtils.toString(content, StandardCharsets.UTF_8) : "";
		} catch (IOException e) {
			Logger.error(e);
			return "";
		}
	}

}