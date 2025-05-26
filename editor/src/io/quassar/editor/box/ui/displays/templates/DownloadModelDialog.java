package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.server.UIFile;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.ModelRelease;

import java.io.File;

public class DownloadModelDialog extends AbstractDownloadModelDialog<EditorBox> {
	private String title;
	private Model model;
	private String release;

	public DownloadModelDialog(EditorBox box) {
		super(box);
	}

	public void title(String title) {
		this.title = title;
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String release) {
		this.release = release;
	}

	public void open() {
		dialog.open();
	}

	@Override
	public void init() {
		super.init();
		download.onExecute(e -> download());
		downloadSilently.onExecute(e -> download());
		dialog.onOpen(e -> refreshDialog());
	}

	private void refreshDialog() {
		dialog.title(title != null ? title : translate("Download %s %s").formatted(ModelHelper.label(model, language(), box()), release));
		ModelRelease modelRelease = model.release(release);
		modelUrl.value(PathHelper.commitUrl(modelRelease, session()));
		copyReleaseUrl.text(PathHelper.commitUrl(modelRelease, session()));
		copyReleaseCommit.text(modelRelease.commit());
	}

	private UIFile download() {
		File release = box().modelManager().release(model, this.release);
		return DisplayHelper.uiFile(ModelHelper.label(model, language(), box()) + "-" + release.getName(), release);
	}
}